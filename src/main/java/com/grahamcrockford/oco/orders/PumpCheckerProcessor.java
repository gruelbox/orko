package com.grahamcrockford.oco.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.AdvancedOrderInfo;
import com.grahamcrockford.oco.api.AdvancedOrderProcessor;
import com.grahamcrockford.oco.core.AdvancedOrderEnqueuer;
import com.grahamcrockford.oco.core.TelegramService;
import one.util.streamex.StreamEx;

@Singleton
public class PumpCheckerProcessor implements AdvancedOrderProcessor<PumpChecker> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PumpCheckerProcessor.class);
  private static final ColumnLogger COLUMN_LOGGER = new ColumnLogger(LOGGER,
    LogColumn.builder().name("#").width(13).rightAligned(false),
    LogColumn.builder().name("Exchange").width(10).rightAligned(false),
    LogColumn.builder().name("Pair").width(10).rightAligned(false),
    LogColumn.builder().name("Operation").width(13).rightAligned(false),
    LogColumn.builder().name("3 tick Mvmt %").width(13).rightAligned(true)
  );

  private final AtomicInteger logRowCount = new AtomicInteger();

  private final TelegramService telegramService;
  private final AdvancedOrderEnqueuer sender;


  @Inject
  public PumpCheckerProcessor(TelegramService telegramService, AdvancedOrderEnqueuer sender) {
    this.telegramService = telegramService;
    this.sender = sender;
  }

  @Override
  public void tick(PumpChecker job, Ticker ticker) throws Exception {

    LinkedList<BigDecimal> linkedList = new LinkedList<>(job.priceHistory());
    linkedList.add(ticker.getLast());
    if (linkedList.size() > 3) {
      linkedList.remove();

      final AdvancedOrderInfo ex = job.basic();

      final int rowCount = logRowCount.getAndIncrement();
      if (rowCount == 0) {
        COLUMN_LOGGER.header();
      }
      if (rowCount == 25) {
        logRowCount.set(0);
      }

      BigDecimal movement = linkedList.getLast().subtract(linkedList.getFirst());
      BigDecimal asPercentage = movement
          .setScale(2, RoundingMode.HALF_UP)
          .multiply(new BigDecimal(100))
          .divide(linkedList.getFirst(), RoundingMode.HALF_UP);

      if (asPercentage.compareTo(BigDecimal.ONE) > 0) {
        if (!StreamEx.of(linkedList)
              .pairMap((a, b) -> a.compareTo(b) > 0)
              .has(true)) {
          telegramService.sendMessage(String.format(
            "Bot [%d] on [%s/%s/%s] detected %s%% pump",
            job.id(),
            ex.exchange(),
            ex.base(),
            ex.counter(),
            asPercentage
          ));
          linkedList.clear();
        };
      }

      COLUMN_LOGGER.line(
          job.id(),
          ex.exchange(),
          ex.pairName(),
          "Pump checker",
          asPercentage
        );
    }

    // Queue up the next check
    sender.enqueueAfterConfiguredDelay(job.toBuilder().priceHistory(linkedList).build());
  }
}