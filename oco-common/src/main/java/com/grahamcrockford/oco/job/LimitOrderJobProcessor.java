package com.grahamcrockford.oco.job;

import java.util.Date;

import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.trade.TradeService;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.exchange.TradeServiceFactory;
import com.grahamcrockford.oco.job.LimitOrderJob.Direction;
import com.grahamcrockford.oco.notification.NotificationService;
import com.grahamcrockford.oco.spi.JobControl;

class LimitOrderJobProcessor implements LimitOrderJob.Processor {

  private final NotificationService notificationService;
  private final TradeServiceFactory tradeServiceFactory;

  private final LimitOrderJob job;

  private TradeService tradeService;
  private LimitOrder order;


  @AssistedInject
  public LimitOrderJobProcessor(@Assisted final LimitOrderJob job,
                                @Assisted final JobControl jobControl,
                                final NotificationService notificationService,
                                final TradeServiceFactory tradeServiceFactory) {
    this.job = job;
    this.notificationService = notificationService;
    this.tradeServiceFactory = tradeServiceFactory;
  }

  /**
   * We do preparatory work in the start method - retries are safe.
   */
  @Override
  public boolean start() {
    this.tradeService = tradeServiceFactory.getForExchange(job.tickTrigger().exchange());
    this.order = new LimitOrder(
        job.direction() == Direction.SELL ? Order.OrderType.ASK : Order.OrderType.BID,
        job.amount(), job.tickTrigger().currencyPair(),
        null, new Date(), job.limitPrice()
    );
    return false;
  }


  /**
   * We do the actual trade in the stop handler to make absolutely sure that
   * the code is never retried.
   */
  @Override
  public void stop() {
    String xChangeOrderId;
    try {
      xChangeOrderId = tradeService.placeLimitOrder(order);
    } catch (Throwable e) {
      reportFailed(job, e);
      return;
    }
    reportSuccess(job,  xChangeOrderId);
  }

  private void reportSuccess(final LimitOrderJob job, String xChangeOrderId) {
    String message = String.format(
        "Order %s placed on %s %s/%s market: %s %s at %s",
        xChangeOrderId,
        job.tickTrigger().exchange(),
        job.tickTrigger().base(),
        job.tickTrigger().counter(),
        job.direction().toString().toLowerCase(),
        job.amount().toPlainString(),
        job.limitPrice().toPlainString()
      );
    notificationService.info(message);
  }

  private void reportFailed(final LimitOrderJob job, Throwable e) {
    String message = String.format(
        "Error placing order on %s %s/%s market: %s %s at %s (%s)",
        job.tickTrigger().exchange(),
        job.tickTrigger().base(),
        job.tickTrigger().counter(),
        job.direction().toString().toLowerCase(),
        job.amount().toPlainString(),
        job.limitPrice().toPlainString(),
        e.getMessage()
      );
    notificationService.error(message, e);
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(LimitOrderJob.Processor.class, LimitOrderJobProcessor.class)
          .build(LimitOrderJob.Processor.Factory.class));
    }
  }
}