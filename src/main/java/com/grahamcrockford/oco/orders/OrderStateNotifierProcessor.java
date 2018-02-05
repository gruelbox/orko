package com.grahamcrockford.oco.orders;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.AdvancedOrderInfo;
import com.grahamcrockford.oco.api.AdvancedOrderProcessor;
import com.grahamcrockford.oco.core.AdvancedOrderEnqueuer;
import com.grahamcrockford.oco.core.TelegramService;
import com.grahamcrockford.oco.core.TradeServiceFactory;

@Singleton
public class OrderStateNotifierProcessor implements AdvancedOrderProcessor<OrderStateNotifier> {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderStateNotifierProcessor.class);
  private static final ColumnLogger COLUMN_LOGGER = new ColumnLogger(LOGGER,
    LogColumn.builder().name("#").width(13).rightAligned(false),
    LogColumn.builder().name("Exchange").width(10).rightAligned(false),
    LogColumn.builder().name("Pair").width(10).rightAligned(false),
    LogColumn.builder().name("Operation").width(13).rightAligned(false),
    LogColumn.builder().name("Order id").width(50).rightAligned(false),
    LogColumn.builder().name("Status").width(16).rightAligned(false),
    LogColumn.builder().name("Amount").width(13).rightAligned(true),
    LogColumn.builder().name("Filled").width(13).rightAligned(true),
    LogColumn.builder().name("Description").width(30).rightAligned(false)
  );

  private final TelegramService telegramService;
  private final TradeServiceFactory tradeServiceFactory;
  private final AdvancedOrderEnqueuer advancedOrderEnqueuer;

  private final AtomicInteger logRowCount = new AtomicInteger();


  @Inject
  public OrderStateNotifierProcessor(final TelegramService telegramService,
                                     final TradeServiceFactory tradeServiceFactory,
                                     final AdvancedOrderEnqueuer advancedOrderEnqueuer) {
    this.telegramService = telegramService;
    this.tradeServiceFactory = tradeServiceFactory;
    this.advancedOrderEnqueuer = advancedOrderEnqueuer;
  }


  @Override
  public void tick(OrderStateNotifier job, Ticker ticker) throws Exception {

    final AdvancedOrderInfo ex = job.basic();

    final int rowCount = logRowCount.getAndIncrement();
    if (rowCount == 0) {
      COLUMN_LOGGER.header();
    }
    if (rowCount == 25) {
      logRowCount.set(0);
    }

    Order.OrderStatus status = null;
    BigDecimal amount = null;
    BigDecimal filled = null;
    boolean exit = false;

    final Collection<Order> matchingOrders = tradeServiceFactory.getForExchange(ex.exchange()).getOrder(job.orderId());
    if (matchingOrders == null || matchingOrders.isEmpty()) {

      telegramService.sendMessage(String.format(
        "Order [%d] on [%s/%s/%s] was not found on the exchange. Giving up.",
        job.id(), ex.exchange(), ex.base(), ex.counter()
      ));
      exit = true;

    } else if (matchingOrders.size() != 1) {

      telegramService.sendMessage(String.format(
        "Order [%d] on [%s/%s/%s] was not unique on the exchange. Giving up.",
        job.id(), ex.exchange(), ex.base(), ex.counter()
      ));
      exit = true;

    } else {

      final Order order = Iterables.getOnlyElement(matchingOrders);
      amount = order.getOriginalAmount();
      filled = order.getCumulativeAmount();
      status = order.getStatus();
      switch (order.getStatus()) {
        case PENDING_CANCEL:
        case CANCELED:
        case EXPIRED:
        case PENDING_REPLACE:
        case REPLACED:
        case REJECTED:
          telegramService.sendMessage(String.format(
            "Order [%d] (%s) on [%s/%s/%s] " + status + ". Giving up.",
            job.id(), job.description(), ex.exchange(), ex.base(), ex.counter()
          ));
          exit = true;
          break;
        case FILLED:
        case STOPPED:
          telegramService.sendMessage(String.format(
            "Order [%d] (%s) on [%s/%s/%s] has " + status + ". Average price [%s %s]",
            job.id(), job.description(), ex.exchange(), ex.base(), ex.counter(), order.getAveragePrice(), ex.counter()
          ));
          exit = true;
          break;
        case PENDING_NEW:
        case NEW:
        case PARTIALLY_FILLED:
          // In progress so ignore
          break;
        default:
          telegramService.sendMessage(String.format(
            "Order [%d] (%s) on [%s/%s/%s] in unknown status " + status + ". Giving up.",
            job.id(), job.description(), ex.exchange(), ex.base(), ex.counter()
          ));
          exit = true;
          break;
      }
    }

    COLUMN_LOGGER.line(
      job.id(),
      ex.exchange(),
      ex.pairName(),
      "Monitor order",
      job.orderId(),
      status,
      amount,
      filled,
      job.description()
    );

    // If we're not done, queue up another check
    if (!exit) {
      advancedOrderEnqueuer.enqueueAfterConfiguredDelay(job);
    }
  }
}