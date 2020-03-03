/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.job;

import static com.gruelbox.orko.exchange.MarketDataType.TICKER;
import static com.gruelbox.orko.jobrun.spi.Status.FAILURE_TRANSIENT;
import static com.gruelbox.orko.jobrun.spi.Status.SUCCESS;
import static com.gruelbox.orko.util.MoreBigDecimals.stripZeros;

import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.exchange.ExchangeEventRegistry;
import com.gruelbox.orko.exchange.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.exchange.MarketDataSubscription;
import com.gruelbox.orko.exchange.TickerEvent;
import com.gruelbox.orko.job.OneCancelsOther.ThresholdAndJob;
import com.gruelbox.orko.jobrun.JobSubmitter;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.jobrun.spi.StatusUpdateService;
import com.gruelbox.orko.notification.Notification;
import com.gruelbox.orko.notification.NotificationLevel;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.SafelyClose;
import com.gruelbox.orko.util.SafelyDispose;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.atomic.AtomicBoolean;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OneCancelsOtherProcessor implements OneCancelsOther.Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(OneCancelsOtherProcessor.class);

  private static final ColumnLogger COLUMN_LOGGER =
      new ColumnLogger(
          LOGGER,
          LogColumn.builder().name("#").width(24).rightAligned(false),
          LogColumn.builder().name("Exchange").width(12).rightAligned(false),
          LogColumn.builder().name("Pair").width(10).rightAligned(false),
          LogColumn.builder().name("Operation").width(13).rightAligned(false),
          LogColumn.builder().name("Low target").width(13).rightAligned(true),
          LogColumn.builder().name("Bid").width(13).rightAligned(true),
          LogColumn.builder().name("High target").width(13).rightAligned(true));

  private final JobSubmitter jobSubmitter;
  private final StatusUpdateService statusUpdateService;
  private final NotificationService notificationService;
  private final ExchangeEventRegistry exchangeEventRegistry;
  private final JobControl jobControl;
  private final ExchangeService exchangeService;
  private final Transactionally transactionally;

  // Validate at most once every 20 seconds (in practice, whenever we get a tick
  // and a permit is available)
  private final RateLimiter validationTick = RateLimiter.create(0.05);

  private volatile OneCancelsOther job;
  private volatile boolean done;

  private ExchangeEventSubscription subscription;
  private Disposable disposable;

  @AssistedInject
  OneCancelsOtherProcessor(
      @Assisted OneCancelsOther job,
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
    if (!exchangeService.exchangeSupportsPair(
        job.tickTrigger().exchange(), job.tickTrigger().currencyPair())) {
      notificationService.error("Cancelling job as currency no longer supported: " + job);
      return Status.FAILURE_PERMANENT;
    }
    subscription =
        exchangeEventRegistry.subscribe(MarketDataSubscription.create(job.tickTrigger(), TICKER));
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
      if (!done) tickInner(tickerEvent);
    } catch (Exception t) {
      String message =
          String.format(
              "One-cancels-other on %s %s/%s market temporarily failed with error: %s",
              job.tickTrigger().exchange(),
              job.tickTrigger().base(),
              job.tickTrigger().counter(),
              t.getMessage());
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
        job.high() == null ? "-" : job.high().threshold());

    if (validationTick.tryAcquire() && !validateJobs()) return;

    if (job.low() != null && ticker.getBid().compareTo(job.low().threshold()) <= 0) {

      transactionally.run(
          () -> {
            notificationService.send(
                Notification.create(
                    String.format(
                        "One-cancels-other on %s %s/%s market hit low threshold (%s < %s)",
                        job.tickTrigger().exchange(),
                        job.tickTrigger().base(),
                        job.tickTrigger().counter(),
                        stripZeros(ticker.getBid()).toPlainString(),
                        stripZeros(job.low().threshold()).toPlainString()),
                    job.verbose() ? NotificationLevel.ALERT : NotificationLevel.INFO));

            jobSubmitter.submitNewUnchecked(job.low().job());
            done = true;
            jobControl.finish(SUCCESS);
          });

    } else if (job.high() != null && ticker.getBid().compareTo(job.high().threshold()) >= 0) {

      transactionally.run(
          () -> {
            notificationService.send(
                Notification.create(
                    String.format(
                        "One-cancels-other on %s %s/%s market hit high threshold (%s > %s)",
                        job.tickTrigger().exchange(),
                        job.tickTrigger().base(),
                        job.tickTrigger().counter(),
                        stripZeros(ticker.getBid()).toPlainString(),
                        stripZeros(job.high().threshold()).toPlainString()),
                    job.verbose() ? NotificationLevel.ALERT : NotificationLevel.INFO));

            jobSubmitter.submitNewUnchecked(job.high().job());
            done = true;
            jobControl.finish(SUCCESS);
          });
    }
  }

  private boolean validateJobs() {
    LOGGER.debug("Validating {}", job);
    AtomicBoolean success = new AtomicBoolean(true);
    if (job.low() != null) {
      jobSubmitter.validate(
          job.low().job(),
          new JobControl() {

            @Override
            public void replace(Job job) {
              jobControl.replace(
                  OneCancelsOtherProcessor.this
                      .job
                      .toBuilder()
                      .low(
                          ThresholdAndJob.create(
                              OneCancelsOtherProcessor.this.job.low().threshold(), job))
                      .build());
            }

            @Override
            public void finish(Status status) {
              notificationService.error(
                  "Cancelling one-cancels-other due to validation failure on low job");
              jobControl.finish(status);
              success.set(false);
            }
          });
    }
    if (job.high() != null) {
      jobSubmitter.validate(
          job.high().job(),
          new JobControl() {

            @Override
            public void replace(Job job) {
              jobControl.replace(
                  OneCancelsOtherProcessor.this
                      .job
                      .toBuilder()
                      .high(
                          ThresholdAndJob.create(
                              OneCancelsOtherProcessor.this.job.high().threshold(), job))
                      .build());
            }

            @Override
            public void finish(Status status) {
              notificationService.error(
                  "Cancelling one-cancels-other due to validation failure on high job");
              jobControl.finish(status);
              success.set(false);
            }
          });
    }
    return success.get();
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(
          new FactoryModuleBuilder()
              .implement(OneCancelsOther.Processor.class, OneCancelsOtherProcessor.class)
              .build(OneCancelsOther.Processor.ProcessorFactory.class));
    }
  }
}
