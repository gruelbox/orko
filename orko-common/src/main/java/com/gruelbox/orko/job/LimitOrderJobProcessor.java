package com.gruelbox.orko.job;

import static com.gruelbox.orko.jobrun.spi.Status.FAILURE_PERMANENT;
import static com.gruelbox.orko.jobrun.spi.Status.FAILURE_TRANSIENT;
import static com.gruelbox.orko.jobrun.spi.Status.SUCCESS;

import java.util.Date;

import org.knowm.xchange.binance.service.BinanceTradeService;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.exchange.TradeServiceFactory;
import com.gruelbox.orko.job.BinanceExceptionClassifier.DuplicateOrderException;
import com.gruelbox.orko.job.BinanceExceptionClassifier.RetriableBinanceException;
import com.gruelbox.orko.job.LimitOrderJob.Direction;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.jobrun.spi.StatusUpdateService;
import com.gruelbox.orko.notification.NotificationService;

/**
 * Processor for {@link LimitOrderJob}.
 *
 * @author Graham Crockford
 */
class LimitOrderJobProcessor implements LimitOrderJob.Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(LimitOrderJobProcessor.class);

  private final StatusUpdateService statusUpdateService;
  private final NotificationService notificationService;
  private final TradeServiceFactory tradeServiceFactory;

  private volatile LimitOrderJob job;

  private TradeService tradeService;
  private LimitOrder order;

  @AssistedInject
  public LimitOrderJobProcessor(@Assisted final LimitOrderJob job,
                                @Assisted final JobControl jobControl,
                                final StatusUpdateService statusUpdateService,
                                final NotificationService notificationService,
                                final TradeServiceFactory tradeServiceFactory) {
    this.job = job;
    this.statusUpdateService = statusUpdateService;
    this.notificationService = notificationService;
    this.tradeServiceFactory = tradeServiceFactory;
  }

  /**
   * For most exchanges, we have no idempotence protection, so all we do
   * here is to prepare the order, actually performing the order in
   * {@link #stop()} where it won't get retried (accepting that this
   * means we simply have to give up, which isn't ideal).
   *
   * <p>For Binance, though, we can be sure that the exchange won't let
   * us double-submit an order, by using a client order id, so we do
   * it here where retries are allowed.</p>
   */
  @Override
  public Status start() {
    tradeService = tradeServiceFactory.getForExchange(job.tickTrigger().exchange());
    order = new LimitOrder(
        job.direction() == Direction.SELL ? Order.OrderType.ASK : Order.OrderType.BID,
        job.amount(), job.tickTrigger().currencyPair(),
        null, new Date(), job.limitPrice()
    );
    order.addOrderFlag(new BinanceTradeService.BinanceOrderFlags() {
      @Override
      public String getClientId() {
        return job.id();
      }
    });
    if (binance()) {
      return binancePlaceOrder();
    } else {
      return SUCCESS;
    }
  }

  @Override
  public void setReplacedJob(LimitOrderJob job) {
    this.job = job;
  }

  /**
   * For other exchanges, we do the actual trade in the stop handler to make
   * absolutely sure that the code is never retried.
   */
  @Override
  public void stop() {
    if (binance())
      return;
    nonBinancePlaceOrder();
  }

  private Status binancePlaceOrder() {
    String xChangeOrderId = null;
    try {
      xChangeOrderId = BinanceExceptionClassifier.call(() -> tradeService.placeLimitOrder(order));
    } catch (DuplicateOrderException e) {
      reportDuplicate(job);
      return SUCCESS;
    } catch (RetriableBinanceException e) {
      reportFailed(job, e, FAILURE_TRANSIENT);
      return FAILURE_TRANSIENT;
    } catch (Exception e) {
      reportFailed(job, e, FAILURE_PERMANENT);
      return Status.FAILURE_PERMANENT;
    }
    reportSuccess(job, xChangeOrderId);
    return SUCCESS;
  }

  private void nonBinancePlaceOrder() {
    String xChangeOrderId;
    try {
      xChangeOrderId = tradeService.placeLimitOrder(order);
    } catch (Exception e) {
      reportFailed(job, e, FAILURE_PERMANENT);
      return;
    }
    reportSuccess(job, xChangeOrderId);
  }

  private void reportDuplicate(final LimitOrderJob job) {
    String message = String.format(
        "Duplicate order on %s %s/%s market filtered by exchange: %s %s at %s",
        job.tickTrigger().exchange(),
        job.tickTrigger().base(),
        job.tickTrigger().counter(),
        job.direction().toString().toLowerCase(),
        job.amount().toPlainString(),
        job.limitPrice().toPlainString()
      );
    LOGGER.warn(message);
    notificationService.alert(message);
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
    notificationService.alert(message);
  }

  private void reportFailed(final LimitOrderJob job, Throwable e, Status failureStatus) {
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
    statusUpdateService.status(job.id(), failureStatus);
    notificationService.error(message, e);
  }

  private boolean binance() {
    return job.tickTrigger().exchange().equals(Exchanges.BINANCE);
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(LimitOrderJob.Processor.class, LimitOrderJobProcessor.class)
          .build(LimitOrderJob.Processor.ProcessorFactory.class));
    }
  }
}