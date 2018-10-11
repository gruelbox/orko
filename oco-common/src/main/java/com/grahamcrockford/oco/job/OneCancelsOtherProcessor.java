package com.grahamcrockford.oco.job;

import static com.grahamcrockford.oco.notification.NotificationStatus.FAILURE_PERMANENT;
import static com.grahamcrockford.oco.notification.NotificationStatus.FAILURE_TRANSIENT;
import static com.grahamcrockford.oco.notification.NotificationStatus.SUCCESS;

import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.exchange.ExchangeService;
import com.grahamcrockford.oco.marketdata.ExchangeEventRegistry;
import com.grahamcrockford.oco.marketdata.TickerEvent;
import com.grahamcrockford.oco.notification.Notification;
import com.grahamcrockford.oco.notification.NotificationLevel;
import com.grahamcrockford.oco.notification.NotificationService;
import com.grahamcrockford.oco.notification.StatusUpdateService;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.submit.JobSubmitter;

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

  private final OneCancelsOther job;
  private final JobControl jobControl;

  private final ExchangeService exchangeService;

  private volatile boolean done;


  @AssistedInject
  OneCancelsOtherProcessor(@Assisted OneCancelsOther job,
                           @Assisted JobControl jobControl,
                           JobSubmitter jobSubmitter,
                           StatusUpdateService statusUpdateService,
                           NotificationService notificationService,
                           ExchangeEventRegistry exchangeEventRegistry,
                           ExchangeService exchangeService) {
    this.job = job;
    this.jobControl = jobControl;
    this.jobSubmitter = jobSubmitter;
    this.statusUpdateService = statusUpdateService;
    this.notificationService = notificationService;
    this.exchangeEventRegistry = exchangeEventRegistry;
    this.exchangeService = exchangeService;
  }

  @Override
  public boolean start() {
    if (!exchangeService.exchangeSupportsPair(job.tickTrigger().exchange(), job.tickTrigger().currencyPair())) {
      statusUpdateService.status(job.id(), FAILURE_PERMANENT);
      notificationService.error("Cancelling job as currency no longer supported: " + job);
      return false;
    }
    exchangeEventRegistry.registerTicker(job.tickTrigger(), job.id(), this::tick);
    return true;
  }

  @Override
  public void stop() {
    exchangeEventRegistry.unregisterTicker(job.tickTrigger(), job.id());
  }

  private synchronized void tick(TickerEvent tickerEvent) {
    try {
      if (!done)
        tickInner(tickerEvent);
    } catch (Throwable t) {
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

      // This may throw, in which case retry of the job should kick in
      jobSubmitter.submitNewUnchecked(job.low().job());
      statusUpdateService.status(job.id(), SUCCESS);
      done = true;
      jobControl.finish();
      return;

    } else if (job.high() != null && ticker.getBid().compareTo(job.high().threshold()) >= 0) {

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

      // This may throw, in which case retry of the job should kick in
      jobSubmitter.submitNewUnchecked(job.high().job());
      statusUpdateService.status(job.id(), SUCCESS);
      done = true;
      jobControl.finish();
      return;

    }
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(OneCancelsOther.Processor.class, OneCancelsOtherProcessor.class)
          .build(OneCancelsOther.Processor.Factory.class));
    }
  }
}