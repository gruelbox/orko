package com.grahamcrockford.oco.core.jobs;

import java.util.Optional;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.grahamcrockford.oco.api.JobProcessor;
import com.grahamcrockford.oco.api.TickerSpec;
import com.grahamcrockford.oco.core.ExchangeService;
import com.grahamcrockford.oco.core.TelegramService;
import com.grahamcrockford.oco.db.JobAccess;
import com.grahamcrockford.oco.util.Sleep;

public class OneCancelsOtherProcessor implements JobProcessor<OneCancelsOther> {

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

  private final ExchangeService exchangeService;
  private final JobAccess jobAccess;
  private final TelegramService telegramService;
  private final Sleep sleep;

  @Inject
  OneCancelsOtherProcessor(ExchangeService exchangeService, JobAccess jobAccess, TelegramService telegramService, Sleep sleep) {
    this.exchangeService = exchangeService;
    this.jobAccess = jobAccess;
    this.telegramService = telegramService;
    this.sleep = sleep;
  }

  @Override
  public Optional<OneCancelsOther> process(OneCancelsOther job) throws InterruptedException {

    final TickerSpec ex = job.tickTrigger();
    Ticker ticker = exchangeService.fetchTicker(ex);

    COLUMN_LOGGER.line(
        job.id(),
        ex.exchange(),
        ex.pairName(),
        "OCO",
        job.low().threshold(),
        ticker.getBid(),
        job.high().threshold()
      );

    if (ticker.getBid().compareTo(job.low().threshold()) <= 0) {

      LOGGER.info("| - Bid price ({}) hit low threshold ({}). Triggering low action.", ticker.getBid(), job.low().threshold());
      telegramService.sendMessage(String.format(
        "Job [%s] on [%s/%s/%s]: bid price (%s) hit low threshold (%s). Triggering low action.",
        job.id(),
        ex.exchange(),
        ex.base(),
        ex.counter(),
        ticker.getBid(),
        job.low().threshold()
      ));

      jobAccess.insert(job.low().job());

      return Optional.empty();
    } else if (ticker.getBid().compareTo(job.high().threshold()) >= 0) {

      LOGGER.info("| - Bid price ({}) hit high threshold {}). Triggering high action.", ticker.getBid(), job.high().threshold());
      telegramService.sendMessage(String.format(
        "Job [%s] on [%s/%s/%s]: bid price (%s) hit high threshold (%s). Triggering high action.",
        job.id(),
        ex.exchange(),
        ex.base(),
        ex.counter(),
        ticker.getBid(),
        job.high().threshold()
      ));

      jobAccess.insert(job.high().job());
      return Optional.empty();
    }

    sleep.sleep();
    return Optional.of(job);
  }
}
