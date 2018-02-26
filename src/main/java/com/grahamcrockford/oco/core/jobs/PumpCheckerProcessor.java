package com.grahamcrockford.oco.core.jobs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.grahamcrockford.oco.core.api.ExchangeEventRegistry;
import com.grahamcrockford.oco.core.spi.JobProcessor;
import com.grahamcrockford.oco.core.spi.TickerSpec;
import com.grahamcrockford.oco.telegram.TelegramService;
import one.util.streamex.StreamEx;

@Singleton
class PumpCheckerProcessor implements JobProcessor<PumpChecker> {

  private static final BigDecimal TARGET = new BigDecimal("0.5");
  private static final Logger LOGGER = LoggerFactory.getLogger(PumpCheckerProcessor.class);
  private static final ColumnLogger COLUMN_LOGGER = new ColumnLogger(LOGGER,
    LogColumn.builder().name("#").width(24).rightAligned(false),
    LogColumn.builder().name("Exchange").width(12).rightAligned(false),
    LogColumn.builder().name("Pair").width(10).rightAligned(false),
    LogColumn.builder().name("Operation").width(13).rightAligned(false),
    LogColumn.builder().name("3 tick Mvmt %").width(13).rightAligned(true)
  );

  private final TelegramService telegramService;
  private final ExchangeEventRegistry tickerRegistry;

  @Inject
  public PumpCheckerProcessor(TelegramService telegramService, ExchangeEventRegistry tickerRegistry) {
    this.telegramService = telegramService;
    this.tickerRegistry = tickerRegistry;
  }

  @Override
  public void start(PumpChecker job, Consumer<PumpChecker> onUpdate, Runnable onFinished) {
    tickerRegistry.registerTicker(job.tickTrigger(), job.id(), ticker -> process(job, ticker, onUpdate));
  }

  @Override
  public void stop(PumpChecker job) {
    tickerRegistry.unregisterTicker(job.tickTrigger(), job.id());
  }

  private void process(PumpChecker job, Ticker ticker, Consumer<PumpChecker> onUpdate) {
    final TickerSpec ex = job.tickTrigger();

    BigDecimal asPercentage = BigDecimal.ZERO;
    LinkedList<BigDecimal> linkedList = new LinkedList<>(job.priceHistory());

    LOGGER.debug("Current price history: {}", linkedList);

    linkedList.add(ticker.getLast());
    if (linkedList.size() > 3) {
      linkedList.remove();

      BigDecimal movement = linkedList.getLast().subtract(linkedList.getFirst());

      LOGGER.debug("Movement: {}", movement);

      asPercentage = new BigDecimal(movement.doubleValue() * 100 / linkedList.getFirst().doubleValue()).setScale(5, RoundingMode.HALF_UP);

      LOGGER.debug("As %: {}", asPercentage);

      if (asPercentage.compareTo(TARGET) > 0) {
        if (!StreamEx.of(linkedList)
              .pairMap((a, b) -> a.compareTo(b) > 0)
              .has(true)) {
          String message = String.format(
              "Job [%s] on [%s/%s/%s] detected %s%% pump",
              job.id(),
              ex.exchange(),
              ex.base(),
              ex.counter(),
              asPercentage
            );
          LOGGER.info(message);
          telegramService.sendMessage(message);
          linkedList.clear();
        };
      } else if (asPercentage.compareTo(TARGET.negate()) < 0) {
        if (!StreamEx.of(linkedList)
            .pairMap((a, b) -> a.compareTo(b) < 0)
            .has(true)) {
          String message = String.format(
              "Job [%s] on [%s/%s/%s] detected %s%% dump",
              job.id(),
              ex.exchange(),
              ex.base(),
              ex.counter(),
              asPercentage
            );
          LOGGER.info(message);
          telegramService.sendMessage(message);
          linkedList.clear();
        };
      }
    }

    LOGGER.debug("New price history: {}", linkedList);

    COLUMN_LOGGER.line(
        job.id(),
        ex.exchange(),
        ex.pairName(),
        "Pump checker",
        asPercentage
      );

    onUpdate.accept(job.toBuilder().priceHistory(linkedList).build());
  }
}