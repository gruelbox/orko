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
import com.gruelbox.orko.jobrun.JobSubmitter;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.jobrun.spi.StatusUpdateService;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.marketdata.MarketDataSubscription;
import com.gruelbox.orko.marketdata.TickerEvent;
import com.gruelbox.orko.notification.Notification;
import com.gruelbox.orko.notification.NotificationLevel;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.SafelyClose;
import com.gruelbox.orko.util.SafelyDispose;

import io.reactivex.disposables.Disposable;

class OneCancelsOtherProcessor implements OneCancelsOther.Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(OneCancelsOtherProcessor.class);

  private static final ColumnLogger COLUMN_LOGGER = new ColumnLogger(LOGGER,
    LogColumn.builder().name("#").width(24).rightAligned(false),
    LogColumn.builder().name("Exchange").width(12).rightAligned(false),
    LogColumn.builder().name("Pair").width(10).rightAligned(false),
    LogColumn.builder().name("Operation").width(13).rightAligned(false),
    LogColumn.builder().name("Low target").width(13).rightAligned(true),
    LogColumn.builder().name("Bid").width(13).rightAligned(true),
    LogColumn.builder().name("High target").width(13).rightAligned(true)
  );

  private final JobSubmitter jobSubmitter;
  private final StatusUpdateService statusUpdateService;
  private final NotificationService notificationService;
  private final ExchangeEventRegistry exchangeEventRegistry;
  private final JobControl jobControl;
  private final ExchangeService exchangeService;

  private volatile OneCancelsOther job;
  private volatile boolean done;
  private volatile ExchangeEventSubscription subscription;
  private volatile Disposable disposable;

  private final Transactionally transactionally;


  @AssistedInject
  OneCancelsOtherProcessor(@Assisted OneCancelsOther job,
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
  public void setReplacedJob(OneCancelsOther job) {
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
        "One-cancels-other on %s %s/%s market temporarily failed with error: %s",
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
        "OCO",
        job.low() == null ? "-" : job.low().threshold(),
        ticker.getBid(),
        job.high() == null ? "-" : job.high().threshold()
      );

    if (job.low() != null && ticker.getBid().compareTo(job.low().threshold()) <= 0) {

      transactionally.run(() -> {

        notificationService.send(
          Notification.create(
            String.format(
              "One-cancels-other on %s %s/%s market hit low threshold (%s < %s)",
              job.tickTrigger().exchange(),
              job.tickTrigger().base(),
              job.tickTrigger().counter(),
              ticker.getBid(),
              job.low().threshold()
            ),
            job.verbose() ? NotificationLevel.ALERT : NotificationLevel.INFO
          )
        );

        jobSubmitter.submitNewUnchecked(job.low().job());
        done = true;
        jobControl.finish(SUCCESS);

      });

    } else if (job.high() != null && ticker.getBid().compareTo(job.high().threshold()) >= 0) {

      transactionally.run(() -> {

        notificationService.send(
          Notification.create(
            String.format(
              "One-cancels-other on %s %s/%s market hit high threshold (%s > %s)",
              job.tickTrigger().exchange(),
              job.tickTrigger().base(),
              job.tickTrigger().counter(),
              ticker.getBid(),
              job.high().threshold()
            ),
            job.verbose() ? NotificationLevel.ALERT : NotificationLevel.INFO
          )
        );

        jobSubmitter.submitNewUnchecked(job.high().job());
        done = true;
        jobControl.finish(SUCCESS);

      });

    }
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(OneCancelsOther.Processor.class, OneCancelsOtherProcessor.class)
          .build(OneCancelsOther.Processor.ProcessorFactory.class));
    }
  }
}