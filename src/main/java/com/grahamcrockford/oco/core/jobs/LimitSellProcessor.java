package com.grahamcrockford.oco.core.jobs;

import java.util.Date;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.core.api.JobSubmitter;
import com.grahamcrockford.oco.core.api.TradeServiceFactory;
import com.grahamcrockford.oco.core.spi.JobControl;
import com.grahamcrockford.oco.core.spi.JobProcessor;
import com.grahamcrockford.oco.core.spi.TickerSpec;
import com.grahamcrockford.oco.telegram.TelegramService;

class LimitSellProcessor implements JobProcessor<LimitSell> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LimitSellProcessor.class);

  private final TelegramService telegramService;
  private final JobSubmitter jobSubmitter;
  private final TradeServiceFactory tradeServiceFactory;

  private final LimitSell job;

  private TradeService tradeService;
  private LimitOrder order;


  @AssistedInject
  public LimitSellProcessor(@Assisted final LimitSell job,
                            @Assisted final JobControl jobControl,
                            final TelegramService telegramService,
                            final TradeServiceFactory tradeServiceFactory,
                            final JobSubmitter jobSubmitter) {
    this.job = job;
    this.telegramService = telegramService;
    this.tradeServiceFactory = tradeServiceFactory;
    this.jobSubmitter = jobSubmitter;
  }

  /**
   * We do preparatory work in the start method - retries are safe.
   */
  @Override
  public boolean start() {
    this.tradeService = tradeServiceFactory.getForExchange(job.tickTrigger().exchange());
    this.order = new LimitOrder(Order.OrderType.ASK, job.amount(), job.tickTrigger().currencyPair(), null, new Date(), job.limitPrice());
    return false;
  }


  /**
   * We do the actual trade n the stop handler to make absolutely sure that
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

    // Spawn a new job to monitor the progress of the order
    jobSubmitter.submitNew(OrderStateNotifier.builder()
        .exchange(job.tickTrigger().exchange())
        .description("Stop")
        .orderId(xChangeOrderId)
        .build());
  }

  private void reportSuccess(final LimitSell job, String xChangeOrderId) {
    final TickerSpec ex = job.tickTrigger();

    LOGGER.info("| - Order [{}] placed.", xChangeOrderId);
    telegramService.sendMessage(String.format(
      "Bot [%s] on [%s/%s/%s] placed limit sell [%s] at [%s]",
      job.id(),
      ex.exchange(),
      ex.base(),
      ex.counter(),
      xChangeOrderId,
      job.limitPrice()
    ));
  }

  private void reportFailed(final LimitSell job, Throwable e) {
    final TickerSpec ex = job.tickTrigger();

    LOGGER.error("| - Order failed to be placed. Giving up.", e);
    telegramService.sendMessage(String.format(
      "Bot [%s] on [%s/%s/%s] FAILED to place limit sell at [%s]. Cannot continue - it might have worked: %s",
      job.id(),
      ex.exchange(),
      ex.base(),
      ex.counter(),
      job.limitPrice(),
      e.getMessage()
    ));
  }


  public interface Factory extends JobProcessor.Factory<LimitSell> {}

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(new TypeLiteral<JobProcessor<LimitSell>>() {}, LimitSellProcessor.class)
          .build(Factory.class));
    }
  }
}