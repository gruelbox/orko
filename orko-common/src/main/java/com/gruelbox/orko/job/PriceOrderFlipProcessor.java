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

import static com.gruelbox.orko.jobrun.spi.Status.FAILURE_TRANSIENT;
import static com.gruelbox.orko.jobrun.spi.Status.SUCCESS;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;

import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.job.PriceOrderFlip.State;
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

class PriceOrderFlipProcessor implements PriceOrderFlip.Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(PriceOrderFlipProcessor.class);

  private static final ColumnLogger COLUMN_LOGGER = new ColumnLogger(LOGGER,
    LogColumn.builder().name("#").width(24).rightAligned(false),
    LogColumn.builder().name("Exchange").width(12).rightAligned(false),
    LogColumn.builder().name("Pair").width(10).rightAligned(false),
    LogColumn.builder().name("Operation").width(10).rightAligned(false),
    LogColumn.builder().name("Flip price").width(13).rightAligned(true),
    LogColumn.builder().name("State").width(5).rightAligned(false),
    LogColumn.builder().name("Order id").width(100).rightAligned(false)
  );

  private final JobSubmitter jobSubmitter;
  private final StatusUpdateService statusUpdateService;
  private final NotificationService notificationService;
  private final ExchangeEventRegistry exchangeEventRegistry;
  private final JobControl jobControl;
  private final ExchangeService exchangeService;

  private volatile PriceOrderFlip job;
  private volatile boolean done;
  private volatile ExchangeEventSubscription subscription;
  private volatile Disposable disposable;

  private final Transactionally transactionally;


  @AssistedInject
  PriceOrderFlipProcessor(@Assisted PriceOrderFlip job,
                           @Assisted JobControl jobControl,
                           JobSubmitter jobSubmitter,
                           StatusUpdateService statusUpdateService,
                           NotificationService notificationService,
                           ExchangeEventRegistry exchangeEventRegistry,
                           ExchangeService exchangeService,
                           Transactionally transactionally) {
    this.job = job;
    this.jobControl = jobControl;
    this.jobSubmitter = jobSubmitter;
    this.statusUpdateService = statusUpdateService;
    this.notificationService = notificationService;
    this.exchangeEventRegistry = exchangeEventRegistry;
    this.exchangeService = exchangeService;
    this.transactionally = transactionally;
  }

  @Override
  public Status start() {
    if (!exchangeService.exchangeSupportsPair(job.tickTrigger().exchange(), job.tickTrigger().currencyPair())) {
      notificationService.error("Cancelling job as currency no longer supported: " + job);
      return Status.FAILURE_PERMANENT;
    }
    subscription = exchangeEventRegistry.subscribe(MarketDataSubscription.create(job.tickTrigger(), TICKER));
    disposable = subscription.getTickers().subscribe(this::tick);
    return Status.RUNNING;
  }

  @Override
  public void setReplacedJob(PriceOrderFlip job) {
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
        tickInner(tickerEvent);
    } catch (Exception t) {
      String message = String.format(
        "Price order flip on %s %s/%s market temporarily failed with error: %s",
        job.tickTrigger().exchange(),
        job.tickTrigger().base(),
        job.tickTrigger().counter(),
        t.getMessage()
      );
      LOGGER.error(message, t);
      statusUpdateService.status(job.id(), FAILURE_TRANSIENT);
      notificationService.error(message, t);
    }
  }

  private void tickInner(TickerEvent tickerEvent) {

    final Ticker ticker = tickerEvent.ticker();
    final TickerSpec ex = job.tickTrigger();

    COLUMN_LOGGER.line(
        job.id(),
        ex.exchange(),
        ex.pairName(),
        "Order flip",
        job.flipPrice(),
        job.state(),
        job.activeOrderId()
      );

    if ((job.state() != State.INACTIVE) && jobNoLongerExists()) {
      done = true;
      jobControl.finish(SUCCESS);
      return;
    }

    transactionally.run(() -> {
      if ((job.state() == State.INACTIVE || job.state() == State.HIGH_ACTIVE) && ticker.getBid().compareTo(job.flipPrice()) <= 0) {
        if (job.state() == State.HIGH_ACTIVE) {
          // TODO cancel high order
        }
        // TODO submit low oder
      }
      if ((job.state() == State.INACTIVE || job.state() == State.LOW_ACTIVE) && ticker.getBid().compareTo(job.flipPrice()) >= 0) {
        transactionally.run(() -> {
          if (job.state() == State.LOW_ACTIVE) {
            // TODO cancel low order
          }
          // TODO submit high order
        });
      }
    });
  }

  private boolean jobNoLongerExists() {
    // TODO Auto-generated method stub
    return false;
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(PriceOrderFlip.Processor.class, PriceOrderFlipProcessor.class)
          .build(PriceOrderFlip.Processor.ProcessorFactory.class));
    }
  }
}