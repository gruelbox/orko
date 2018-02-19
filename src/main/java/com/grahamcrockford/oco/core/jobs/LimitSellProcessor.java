package com.grahamcrockford.oco.core.jobs;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import javax.inject.Inject;

import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.JobProcessor;
import com.grahamcrockford.oco.api.TickerSpec;
import com.grahamcrockford.oco.core.TelegramService;
import com.grahamcrockford.oco.core.TradeServiceFactory;
import com.grahamcrockford.oco.db.JobAccess;

@Singleton
public class LimitSellProcessor implements JobProcessor<LimitSell> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LimitSellProcessor.class);

  private final TelegramService telegramService;
  private final TradeServiceFactory tradeServiceFactory;
  private final JobAccess advancedOrderAccess;


  @Inject
  public LimitSellProcessor(final TelegramService telegramService,
                            final TradeServiceFactory tradeServiceFactory,
                            final JobAccess advancedOrderAccess) {
    this.telegramService = telegramService;
    this.tradeServiceFactory = tradeServiceFactory;
    this.advancedOrderAccess = advancedOrderAccess;
  }

  @Override
  public Optional<LimitSell> process(final LimitSell job) throws InterruptedException {

    String xChangeOrderId = limitSell(job);

    // Spawn a new job to monitor the progress of the order
    advancedOrderAccess.insert(OrderStateNotifier.builder()
        .tickTrigger(job.tickTrigger())
        .description("Stop")
        .orderId(xChangeOrderId)
        .build(), OrderStateNotifier.class);

    return Optional.empty();

  }

  private String limitSell(LimitSell job) {
    final TickerSpec ex = job.tickTrigger();

    LOGGER.info("| - Placing limit sell of [{} {}] at limit price [{} {}]", job.amount(), ex.base(), job.limitPrice(), ex.counter());
    final TradeService tradeService = tradeServiceFactory.getForExchange(ex.exchange());
    final Date timestamp = new Date();
    final LimitOrder order = new LimitOrder(Order.OrderType.ASK, job.amount(), ex.currencyPair(), null, timestamp, job.limitPrice());

    try {
      String xChangeOrderId = tradeService.placeLimitOrder(order);

      LOGGER.info("| - Order [{}] placed.", xChangeOrderId);
      telegramService.sendMessage(String.format(
        "Bot [%s] on [%s/%s/%s] placed limit sell at [%s]",
        job.id(),
        ex.exchange(),
        ex.base(),
        ex.counter(),
        job.limitPrice()
      ));

      return xChangeOrderId;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}