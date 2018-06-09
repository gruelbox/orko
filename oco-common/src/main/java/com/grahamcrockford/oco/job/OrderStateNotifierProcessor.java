package com.grahamcrockford.oco.job;

import static com.grahamcrockford.oco.marketdata.MarketDataType.OPEN_ORDERS;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.stream.Collectors;

import org.knowm.xchange.dto.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.marketdata.ExchangeEventRegistry;
import com.grahamcrockford.oco.marketdata.MarketDataSubscription;
import com.grahamcrockford.oco.marketdata.OpenOrdersEvent;
import com.grahamcrockford.oco.notification.NotificationService;
import com.grahamcrockford.oco.spi.JobControl;
import io.reactivex.disposables.Disposable;

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

  private final NotificationService notificationService;
  private final ExchangeEventRegistry exchangeEventRegistry;
  private final OrderStateNotifier job;
  private final JobControl jobControl;

  private final String subscriberId;
  private volatile Disposable subscription;
  private volatile Order order;


  @AssistedInject
  public OrderStateNotifierProcessor(@Assisted OrderStateNotifier job,
                                     @Assisted JobControl jobControl,
                                     final NotificationService notificationService,
                                     ExchangeEventRegistry exchangeEventRegistry) {
    this.job = job;
    this.jobControl = jobControl;
    this.notificationService = notificationService;
    this.exchangeEventRegistry = exchangeEventRegistry;
    this.subscriberId = "OrderStateNotifierProcessor/" + job.id();
  }

  @Override
  public boolean start() {
    exchangeEventRegistry.changeSubscriptions(subscriberId, MarketDataSubscription.create(job.tickTrigger(), OPEN_ORDERS));
    subscription = exchangeEventRegistry.getOpenOrders(subscriberId).subscribe(this::tick);
    return true;
  }

  @Override
  public void stop() {
    subscription.dispose();
    exchangeEventRegistry.clearSubscriptions(subscriberId);
  }

  @VisibleForTesting
  void tick(OpenOrdersEvent openOrdersEvent) {

    Collection<Order> matchingOrders = openOrdersEvent.openOrders()
        .getAllOpenOrders()
        .stream()
        .filter(o -> o.getId().equals(job.orderId()))
        .collect(Collectors.toList());

    if (matchingOrders.isEmpty()) {

      if (this.order == null) {
        notFoundMessage(job);
      } else  {
        completedOrRemovedMessage(job);
      }
      jobControl.finish();
      return;

    } else if (matchingOrders.size() != 1) {

      notUniqueMessage(job);
      jobControl.finish();
      return;

    } else {
      this.order = matchingOrders.iterator().next();

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

    }
  }

  private void completedOrRemovedMessage(OrderStateNotifier job2) {
    String message = String.format(
        "Order closed: order for %s on [%s %s/%s] (id %s) not found. Was cancelled or filled.",
        order.getOriginalAmount().toPlainString(),
        job.tickTrigger().exchange(),
        job.tickTrigger().base(),
        job.tickTrigger().counter(),
        job.orderId()
      );
    notificationService.info(message);
  }

  private void notUniqueMessage(OrderStateNotifier job) {
    String message = String.format(
      "Order [%s] on [%s] was not unique on the exchange. Giving up.",
      job.orderId(), job.tickTrigger().exchange(), job.tickTrigger().base(), job.tickTrigger().counter()
    );
    notificationService.error(message);
  }

  private void notFoundMessage(OrderStateNotifier job) {
    String message = String.format(
        "Order not found: order id [%s] on [%s %s/%s] not found. Was cancelled or filled.",
        job.orderId(),
        job.tickTrigger().exchange(),
        job.tickTrigger().base(),
        job.tickTrigger().counter()
      );
    notificationService.info(message);
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