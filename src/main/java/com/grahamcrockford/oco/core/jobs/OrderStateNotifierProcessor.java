package com.grahamcrockford.oco.core.jobs;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.knowm.xchange.dto.Order;
import org.knowm.xchange.exceptions.ExchangeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.JobProcessor;
import com.grahamcrockford.oco.api.TickerSpec;
import com.grahamcrockford.oco.core.TelegramService;
import com.grahamcrockford.oco.core.TradeServiceFactory;
import com.grahamcrockford.oco.util.Sleep;

import si.mazi.rescu.HttpStatusExceptionSupport;

@Singleton
public class OrderStateNotifierProcessor implements JobProcessor<OrderStateNotifier> {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderStateNotifierProcessor.class);
  private static final ColumnLogger COLUMN_LOGGER = new ColumnLogger(LOGGER,
    LogColumn.builder().name("#").width(26).rightAligned(false),
    LogColumn.builder().name("Exchange").width(12).rightAligned(false),
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
  private final Sleep sleep;

  private final AtomicInteger logRowCount = new AtomicInteger();


  @Inject
  public OrderStateNotifierProcessor(final TelegramService telegramService,
                                     final TradeServiceFactory tradeServiceFactory,
                                     final Sleep sleep) {
    this.telegramService = telegramService;
    this.tradeServiceFactory = tradeServiceFactory;
    this.sleep = sleep;
  }


  @Override
  public Optional<OrderStateNotifier> process(OrderStateNotifier job) throws InterruptedException {

    final TickerSpec ex = job.tickTrigger();

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

    final Order order = getOrder(job, ex);
    if (order == null) {

      exit = true;

    } else {

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
            "Order [%s] (%s) on [%s/%s/%s] " + status + ". Giving up.",
            job.id(), job.description(), ex.exchange(), ex.base(), ex.counter()
          ));
          exit = true;
          break;
        case FILLED:
        case STOPPED:
          telegramService.sendMessage(String.format(
            "Order [%s] (%s) on [%s/%s/%s] has " + status + ". Average price [%s %s]",
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
            "Order [%s] (%s) on [%s/%s/%s] in unknown status " + status + ". Giving up.",
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

    if (!exit)
      sleep.sleep();

    return exit
        ? Optional.empty()
        : Optional.of(job);
  }

  private Order getOrder(OrderStateNotifier job, final TickerSpec ex) {
    final Collection<Order> matchingOrders;
    try {
      matchingOrders = tradeServiceFactory.getForExchange(ex.exchange()).getOrder(job.orderId());
    } catch (ExchangeException | IOException e) {
      if (e.getCause() instanceof HttpStatusExceptionSupport && ((HttpStatusExceptionSupport)e.getCause()).getHttpStatusCode() == 404) {
        notFoundMessage(job, ex);
        return null;
      }
      throw new RuntimeException(e);
    }

    if (matchingOrders == null || matchingOrders.isEmpty()) {
      notFoundMessage(job, ex);
      return null;
    } else if (matchingOrders.size() != 1) {
      notUniqueMessage(job, ex);
      return null;
    }

    return Iterables.getOnlyElement(matchingOrders);
  }

  private void notUniqueMessage(OrderStateNotifier job, final TickerSpec ex) {
    String message = String.format(
      "Order [%s] on [%s/%s/%s] was not unique on the exchange. Giving up.",
      job.id(), ex.exchange(), ex.base(), ex.counter()
    );
    LOGGER.error(message);
    telegramService.sendMessage(message);
  }

  private void notFoundMessage(OrderStateNotifier job, final TickerSpec ex) {
    String message = String.format(
        "Order [%s] on [%s/%s/%s] was not found on the exchange. It may have been cancelled. Giving up.",
        job.id(), ex.exchange(), ex.base(), ex.counter()
      );
    LOGGER.error(message);
    telegramService.sendMessage(message);
  }
}