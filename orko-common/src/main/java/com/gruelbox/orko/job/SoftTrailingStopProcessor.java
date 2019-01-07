/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gruelbox.orko.job;

import static com.gruelbox.orko.jobrun.spi.Status.FAILURE_PERMANENT;
import static com.gruelbox.orko.jobrun.spi.Status.FAILURE_TRANSIENT;
import static com.gruelbox.orko.jobrun.spi.Status.SUCCESS;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.inject.Inject;

import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.job.LimitOrderJob.Direction;
import com.gruelbox.orko.jobrun.JobSubmitter;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.jobrun.spi.StatusUpdateService;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.marketdata.MarketDataSubscription;
import com.gruelbox.orko.marketdata.TickerEvent;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.SafelyClose;
import com.gruelbox.orko.util.SafelyDispose;

import io.reactivex.disposables.Disposable;

class SoftTrailingStopProcessor implements SoftTrailingStop.Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(SoftTrailingStopProcessor.class);
  private static final ColumnLogger COLUMN_LOGGER = new ColumnLogger(LOGGER,
      LogColumn.builder().name("#").width(26).rightAligned(false),
      LogColumn.builder().name("Exchange").width(12).rightAligned(false),
      LogColumn.builder().name("Pair").width(10).rightAligned(false),
      LogColumn.builder().name("Operation").width(13).rightAligned(false),
      LogColumn.builder().name("Entry").width(13).rightAligned(true),
      LogColumn.builder().name("Stop").width(13).rightAligned(true),
      LogColumn.builder().name("Bid").width(13).rightAligned(true),
      LogColumn.builder().name("Last").width(13).rightAligned(true),
      LogColumn.builder().name("Ask").width(13).rightAligned(true)
  );

  private final StatusUpdateService statusUpdateService;
  private final NotificationService notificationService;
  private final ExchangeService exchangeService;
  private final JobSubmitter jobSubmitter;
  private final JobControl jobControl;
  private final ExchangeEventRegistry exchangeEventRegistry;
  private volatile boolean done;

  private volatile SoftTrailingStop job;
  private volatile ExchangeEventSubscription subscription;
  private volatile Disposable disposable;
  private final Transactionally transactionally;


  @Inject
  public SoftTrailingStopProcessor(@Assisted SoftTrailingStop job,
                                   @Assisted JobControl jobControl,
                                   final StatusUpdateService statusUpdateService,
                                   final NotificationService notificationService,
                                   final ExchangeService exchangeService,
                                   final JobSubmitter jobSubmitter,
                                   final ExchangeEventRegistry exchangeEventRegistry,
                                   final Transactionally transactionally) {
    this.job = job;
    this.jobControl = jobControl;
    this.statusUpdateService = statusUpdateService;
    this.notificationService = notificationService;
    this.exchangeService = exchangeService;
    this.jobSubmitter = jobSubmitter;
    this.exchangeEventRegistry = exchangeEventRegistry;
    this.transactionally = transactionally;
  }

  @Override
  public Status start() {
    subscription = exchangeEventRegistry.subscribe(MarketDataSubscription.create(job.tickTrigger(), TICKER));
    disposable = subscription.getTickers().subscribe(this::tick);
    return Status.RUNNING;
  }

  @Override
  public void setReplacedJob(SoftTrailingStop job) {
    this.job = job;
  }

  @Override
  public void stop() {
    SafelyDispose.of(disposable);
    SafelyClose.the(subscription);
  }

  private synchronized void tick(TickerEvent tickerEvent) {
    try {
      if (!done)
        transactionally.run(() -> tickTransaction(tickerEvent));
    } catch (Exception t) {
      String message = String.format(
        "Trailing stop on %s %s/%s market temporarily failed with error: %s",
        job.tickTrigger().exchange(),
        job.tickTrigger().base(),
        job.tickTrigger().counter(),
        t.getMessage()
      );
      LOGGER.error(message, t);
      statusUpdateService.status(job.id(), FAILURE_TRANSIENT, message);
    }
  }

  private void tickTransaction(TickerEvent tickerEvent) {

    final Ticker ticker = tickerEvent.ticker();
    final TickerSpec ex = job.tickTrigger();

    if (ticker.getAsk() == null) {
      statusUpdateService.status(job.id(), FAILURE_PERMANENT);
      notificationService.error(String.format("Market %s/%s/%s has no sellers!", ex.exchange(), ex.base(), ex.counter()));
      return;
    }
    if (ticker.getBid() == null) {
      statusUpdateService.status(job.id(), FAILURE_PERMANENT);
      notificationService.error(String.format("Market %s/%s/%s has no buyers!", ex.exchange(), ex.base(), ex.counter()));
      return;
    }

    final CurrencyPairMetaData currencyPairMetaData = exchangeService.fetchCurrencyPairMetaData(ex);

    logStatus(job, ticker, currencyPairMetaData);

    BigDecimal stopPrice = stopPrice(job, currencyPairMetaData);

    // If we've hit the stop price, we're done
    if ((job.direction().equals(Direction.SELL) && ticker.getBid().compareTo(stopPrice) <= 0) ||
        (job.direction().equals(Direction.BUY) && ticker.getAsk().compareTo(stopPrice) >= 0)) {

      notificationService.info(String.format(
        "Trailing stop on %s %s/%s market hit stop price (%s < %s)",
        job.tickTrigger().exchange(),
        job.tickTrigger().base(),
        job.tickTrigger().counter(),
        ticker.getBid(),
        stopPrice
      ));

      jobSubmitter.submitNewUnchecked(LimitOrderJob.builder()
          .tickTrigger(ex)
          .direction(job.direction())
          .amount(job.amount())
          .limitPrice(job.limitPrice())
          .build());

      jobControl.finish(SUCCESS);
      done = true;
      return;
    }

    if (job.direction().equals(Direction.SELL) && ticker.getBid().compareTo(job.lastSyncPrice()) > 0) {
      jobControl.replace(
        job.toBuilder()
          .lastSyncPrice(ticker.getBid())
          .stopPrice(job.stopPrice().add(ticker.getBid()).subtract(job.lastSyncPrice()))
          .build()
      );
      return;
    }

    if (job.direction().equals(Direction.BUY) && ticker.getAsk().compareTo(job.lastSyncPrice()) < 0 ) {
      jobControl.replace(
        job.toBuilder()
          .lastSyncPrice(ticker.getAsk())
          .stopPrice(job.stopPrice().add(ticker.getAsk()).subtract(job.lastSyncPrice()))
          .build()
      );
    }
  }

  private void logStatus(final SoftTrailingStop trailingStop, final Ticker ticker, CurrencyPairMetaData currencyPairMetaData) {
    final TickerSpec ex = trailingStop.tickTrigger();
    COLUMN_LOGGER.line(
      trailingStop.id(),
      ex.exchange(),
      ex.pairName(),
      "Trailing " + trailingStop.direction(),
      trailingStop.startPrice().setScale(currencyPairMetaData.getPriceScale(), HALF_UP),
      stopPrice(trailingStop, currencyPairMetaData),
      ticker.getBid().setScale(currencyPairMetaData.getPriceScale(), HALF_UP),
      ticker.getLast().setScale(currencyPairMetaData.getPriceScale(), HALF_UP),
      ticker.getAsk().setScale(currencyPairMetaData.getPriceScale(), HALF_UP)
    );
  }

  private BigDecimal stopPrice(SoftTrailingStop trailingStop, CurrencyPairMetaData currencyPairMetaData) {
    return trailingStop.stopPrice().setScale(currencyPairMetaData.getPriceScale(), RoundingMode.HALF_UP);
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(SoftTrailingStop.Processor.class, SoftTrailingStopProcessor.class)
          .build(SoftTrailingStop.Processor.ProcessorFactory.class));
    }
  }
}