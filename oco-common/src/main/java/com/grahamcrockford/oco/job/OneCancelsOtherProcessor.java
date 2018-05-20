package com.grahamcrockford.oco.job;

import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.marketdata.ExchangeEventRegistry;
import com.grahamcrockford.oco.marketdata.TickerEvent;
import com.grahamcrockford.oco.notification.NotificationService;
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
  private final NotificationService notificationService;
  private final ExchangeEventRegistry exchangeEventRegistry;

  private final OneCancelsOther job;
  private final JobControl jobControl;

  @AssistedInject
  OneCancelsOtherProcessor(@Assisted OneCancelsOther job,
                           @Assisted JobControl jobControl,
                           JobSubmitter jobSubmitter,
                           NotificationService notificationService,
                           ExchangeEventRegistry exchangeEventRegistry) {
    this.job = job;
    this.jobControl = jobControl;
    this.jobSubmitter = jobSubmitter;
    this.notificationService = notificationService;
    this.exchangeEventRegistry = exchangeEventRegistry;
  }

  @Override
  public boolean start() {
    exchangeEventRegistry.registerTicker(job.tickTrigger(), job.id(), this::tick);
    return true;
  }

  @Override
  public void stop() {
    exchangeEventRegistry.unregisterTicker(job.tickTrigger(), job.id());
  }

  private void tick(TickerEvent tickerEvent) {

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

      if (job.verbose()) {
        notificationService.info(String.format(
          "Job [%s] on [%s/%s/%s]: bid price (%s) hit low threshold (%s). Triggering low action.",
          job.id(),
          ex.exchange(),
          ex.base(),
          ex.counter(),
          ticker.getBid(),
          job.low().threshold()
        ));
      }

      // This may throw, in which case retry of the job should kick in
      jobSubmitter.submitNewUnchecked(job.low().job());
      jobControl.finish();
      return;

    } else if (job.high() != null && ticker.getBid().compareTo(job.high().threshold()) >= 0) {

      if (job.verbose()) {
        notificationService.info(String.format(
          "Job [%s] on [%s/%s/%s]: bid price (%s) hit high threshold (%s). Triggering high action.",
          job.id(),
          ex.exchange(),
          ex.base(),
          ex.counter(),
          ticker.getBid(),
          job.high().threshold()
        ));
      }

      // This may throw, in which case retry of the job should kick in
      jobSubmitter.submitNewUnchecked(job.high().job());
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