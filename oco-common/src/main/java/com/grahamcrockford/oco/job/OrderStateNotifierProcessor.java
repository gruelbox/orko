package com.grahamcrockford.oco.job;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.knowm.xchange.binance.service.BinanceQueryOrderParams;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.exchange.TradeServiceFactory;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.KeepAliveEvent;
import com.grahamcrockford.oco.telegram.TelegramService;

import si.mazi.rescu.HttpStatusExceptionSupport;

class OrderStateNotifierProcessor implements OrderStateNotifier.Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderStateNotifierProcessor.class);
  private static final ColumnLogger COLUMN_LOGGER = new ColumnLogger(LOGGER,
    LogColumn.builder().name("#").width(24).rightAligned(false),
    LogColumn.builder().name("Exchange").width(12).rightAligned(false),
    LogColumn.builder().name("Pair").width(10).rightAligned(false),
    LogColumn.builder().name("Operation").width(13).rightAligned(false),
    LogColumn.builder().name("Order id").width(50).rightAligned(false),
    LogColumn.builder().name("Status").width(16).rightAligned(false),
    LogColumn.builder().name("Amount").width(13).rightAligned(true),
    LogColumn.builder().name("Filled").width(13).rightAligned(true)
  );

  private final TelegramService telegramService;
  private final TradeServiceFactory tradeServiceFactory;
  private final AsyncEventBus asyncEventBus;
  private final OrderStateNotifier job;
  private final JobControl jobControl;

  private final AtomicBoolean couldNotFetchOrderData = new AtomicBoolean();


  @AssistedInject
  public OrderStateNotifierProcessor(@Assisted OrderStateNotifier job,
                                     @Assisted JobControl jobControl,
                                     final TelegramService telegramService,
                                     final TradeServiceFactory tradeServiceFactory,
                                     final AsyncEventBus asyncEventBus) {
    this.job = job;
    this.jobControl = jobControl;
    this.telegramService = telegramService;
    this.tradeServiceFactory = tradeServiceFactory;
    this.asyncEventBus = asyncEventBus;
  }

  @Override
  public boolean start() {
    if (tick()) {
      asyncEventBus.register(this);
      return true;
    } else {
      couldNotFetchOrderData.set(true);
      return false;
    }
  }

  @Override
  public void stop() {
    if (!couldNotFetchOrderData.get())
      asyncEventBus.unregister(this);
  }

  @Subscribe
  @VisibleForTesting
  void process(KeepAliveEvent keepAliveEvent) {
    if (!tick())
      jobControl.finish();
  }

  private boolean tick() {

    final Order order = getOrder(job);
    if (order == null) {

      return false;

    } else {

      BigDecimal amount = order.getOriginalAmount();
      BigDecimal filled = order.getCumulativeAmount();
      Order.OrderStatus status = order.getStatus();

      COLUMN_LOGGER.line(
        job.id(),
        job.tickTrigger().exchange(),
        job.tickTrigger().pairName(),
        "Monitor order",
        job.orderId(),
        status,
        amount,
        filled
      );

      switch (order.getStatus()) {
        case PENDING_CANCEL:
        case CANCELED:
        case EXPIRED:
        case PENDING_REPLACE:
        case REPLACED:
        case REJECTED:
          telegramService.sendMessage(String.format(
            "Order [%s] on [%s/%s/%s] %s. Giving up.",
            job.orderId(),
            job.tickTrigger().exchange(), job.tickTrigger().base(), job.tickTrigger().counter(),
            status
          ));
          return false;
        case FILLED:
        case STOPPED:
          telegramService.sendMessage(String.format(
            "Order [%s] on [%s/%s/%s] has %s. Average price [%s]",
            job.orderId(), job.tickTrigger().exchange(), job.tickTrigger().base(), job.tickTrigger().counter(),
            status, order.getAveragePrice()
          ));
          return false;
        case PENDING_NEW:
        case NEW:
        case PARTIALLY_FILLED:
          // In progress so ignore
          return true;
        default:
          telegramService.sendMessage(String.format(
            "Order [%s] on [%s/%s/%s] in unknown status %s. Giving up.",
            job.orderId(), job.tickTrigger().exchange(), job.tickTrigger().base(), job.tickTrigger().counter(),
            status
          ));
          return false;
      }
    }
  }

  private Order getOrder(OrderStateNotifier job) {
    final Collection<Order> matchingOrders;
    try {
      matchingOrders = tradeServiceFactory.getForExchange(job.tickTrigger().exchange())
          .getOrder(new BinanceQueryOrderParams(job.tickTrigger().currencyPair(), job.orderId()));
    } catch (NotAvailableFromExchangeException e) {
      return getOrders(job);
    } catch (ArithmeticException e) {
      gdaxBugMessage(job);
      return null;
    } catch (ExchangeException | IOException e) {
      if (e.getCause() instanceof HttpStatusExceptionSupport && ((HttpStatusExceptionSupport)e.getCause()).getHttpStatusCode() == 404) {
        notFoundMessage(job);
        return null;
      } else {
        throw new RuntimeException(e);
      }
    }

    if (matchingOrders == null || matchingOrders.isEmpty()) {
      notFoundMessage(job);
      return null;
    } else if (matchingOrders.size() != 1) {
      notUniqueMessage(job);
      return null;
    }

    return Iterables.getOnlyElement(matchingOrders);
  }

  private Order getOrders(OrderStateNotifier job2) {
    final Collection<Order> matchingOrders;
    try {
      OpenOrders openOrders = tradeServiceFactory
        .getForExchange(job.tickTrigger().exchange())
        .getOpenOrders(new DefaultOpenOrdersParamCurrencyPair(job.tickTrigger().currencyPair()));
      matchingOrders = openOrders
        .getAllOpenOrders()
        .stream()
        .filter(o -> o.getId().equals(job.orderId()))
        .collect(Collectors.toList());
    } catch (NotAvailableFromExchangeException e) {
      notSupportedMessage(job);
      return null;
    } catch (ExchangeException | IOException e) {
      if (e.getCause() instanceof HttpStatusExceptionSupport && ((HttpStatusExceptionSupport)e.getCause()).getHttpStatusCode() == 404) {
        notFoundMessage(job);
        return null;
      } else {
        throw new RuntimeException(e);
      }
    }

    if (matchingOrders.isEmpty()) {
      notFoundMessage(job);
      return null;
    } else if (matchingOrders.size() != 1) {
      notUniqueMessage(job);
      return null;
    }

    return Iterables.getOnlyElement(matchingOrders);
  }

  private void gdaxBugMessage(OrderStateNotifier job) {
    String message = String.format(
        "Order [%s] on [%s/%s/%s] can't be checked. There's a bug in the GDAX access library which prevents it. It'll be fixed soon.",
        job.orderId(), job.tickTrigger().exchange(), job.tickTrigger().base(), job.tickTrigger().counter()
      );
      LOGGER.warn(message);
      telegramService.sendMessage(message);
  }


  private void notUniqueMessage(OrderStateNotifier job) {
    String message = String.format(
      "Order [%s] on [%s] was not unique on the exchange. Giving up.",
      job.orderId(), job.tickTrigger().exchange(), job.tickTrigger().base(), job.tickTrigger().counter()
    );
    LOGGER.error(message);
    telegramService.sendMessage(message);
  }

  private void notFoundMessage(OrderStateNotifier job) {
    String message = String.format(
        "Order [%s] on [%s] was not found on the exchange. It may have been cancelled. Giving up.",
        job.orderId(), job.tickTrigger().exchange(), job.tickTrigger().base(), job.tickTrigger().counter()
      );
    LOGGER.warn(message);
    telegramService.sendMessage(message);
  }

  private void notSupportedMessage(OrderStateNotifier job) {
    String message = String.format(
        "Order [%s] on [%s] can't be checked. The exchange doesn't support order status checks. Giving up.",
        job.orderId(), job.tickTrigger().exchange(), job.tickTrigger().base(), job.tickTrigger().counter()
      );
    LOGGER.warn(message);
    telegramService.sendMessage(message);
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(OrderStateNotifier.Processor.class, OrderStateNotifierProcessor.class)
          .build(OrderStateNotifier.Processor.Factory.class));
    }
  }
}