/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.job;

import static com.gruelbox.orko.exchange.Exchanges.name;
import static com.gruelbox.orko.job.LimitOrderJob.BalanceState.INSUFFICIENT_BALANCE;
import static com.gruelbox.orko.job.LimitOrderJob.BalanceState.SUFFICIENT_BALANCE;
import static com.gruelbox.orko.jobrun.spi.Status.FAILURE_PERMANENT;
import static com.gruelbox.orko.jobrun.spi.Status.FAILURE_TRANSIENT;
import static com.gruelbox.orko.jobrun.spi.Status.SUCCESS;
import static com.gruelbox.orko.util.MoreBigDecimals.stripZeros;
import static java.math.BigDecimal.ZERO;
import static org.apache.commons.lang3.StringUtils.capitalize;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.gruelbox.orko.exchange.ExchangeEventRegistry;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.exchange.MaxTradeAmountCalculator;
import com.gruelbox.orko.exchange.RateController;
import com.gruelbox.orko.exchange.TradeServiceFactory;
import com.gruelbox.orko.job.BinanceExceptionClassifier.DuplicateOrderException;
import com.gruelbox.orko.job.BinanceExceptionClassifier.RetriableBinanceException;
import com.gruelbox.orko.job.LimitOrderJob.Direction;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.jobrun.spi.StatusUpdateService;
import com.gruelbox.orko.jobrun.spi.Validatable;
import com.gruelbox.orko.notification.NotificationService;
import java.math.BigDecimal;
import java.util.Date;
import java.util.function.Consumer;
import org.knowm.xchange.binance.service.BinanceTradeService;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.FundsExceededException;
import org.knowm.xchange.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processor for {@link LimitOrderJob}.
 *
 * @author Graham Crockford
 */
class LimitOrderJobProcessor implements LimitOrderJob.Processor, Validatable {

  private static final Logger LOGGER = LoggerFactory.getLogger(LimitOrderJobProcessor.class);

  private final StatusUpdateService statusUpdateService;
  private final JobControl jobControl;
  private final NotificationService notificationService;
  private final TradeServiceFactory tradeServiceFactory;
  private final RateController rateController;
  private final MaxTradeAmountCalculator calculator;

  private final BigDecimal minimumAmount;

  private volatile LimitOrderJob job;

  private TradeService tradeService;
  private volatile LimitOrder order;

  @AssistedInject
  public LimitOrderJobProcessor(
      @Assisted final LimitOrderJob job,
      @Assisted final JobControl jobControl,
      final StatusUpdateService statusUpdateService,
      final NotificationService notificationService,
      final TradeServiceFactory tradeServiceFactory,
      final ExchangeEventRegistry exchangeEventRegistry,
      final ExchangeService exchangeService,
      final MaxTradeAmountCalculator.Factory calculatorFactory) {
    this.job = job;
    this.jobControl = jobControl;
    this.statusUpdateService = statusUpdateService;
    this.notificationService = notificationService;
    this.tradeServiceFactory = tradeServiceFactory;
    this.calculator = calculatorFactory.create(job.tickTrigger());
    this.rateController = exchangeService.rateController(job.tickTrigger().exchange());
    this.minimumAmount =
        exchangeService
            .get(job.tickTrigger().exchange())
            .getExchangeMetaData()
            .getCurrencyPairs()
            .get(job.tickTrigger().currencyPair())
            .getMinimumAmount();
  }

  /**
   * If the job hasn't been started yet, and is being held in reserve (e.g. by a {@link
   * OneCancelsOther}), periodicaly make sure that the balance is actually available.
   */
  @Override
  public void validate() {
    LOGGER.debug("Validating {}", job);
    setupOrder(message -> {});
    BigDecimal available =
        stripZeros(
            calculator.validOrderAmount(
                job.limitPrice(),
                job.direction().equals(Direction.SELL) ? OrderType.ASK : OrderType.BID));
    BigDecimal amount = stripZeros(job.amount());
    if (amount.compareTo(available) <= 0) {
      if (job.balanceState().equals(INSUFFICIENT_BALANCE)) {
        LOGGER.debug("Sufficient balance. Updating job");
        jobControl.replace(job.toBuilder().balanceState(SUFFICIENT_BALANCE).build());
        String message =
            String.format(
                "%s order in reserve on %s %s/%s with amount %s is now fully executable as the balance is available.",
                capitalize(job.direction().toString().toLowerCase()),
                name(job.tickTrigger().exchange()),
                job.tickTrigger().base(),
                job.tickTrigger().counter(),
                amount.toPlainString());
        notificationService.alert(message);
      }
    } else {
      if (job.balanceState().equals(SUFFICIENT_BALANCE)) {
        LOGGER.debug("Insufficient balance. Updating job");
        String message;
        if (job.direction().equals(Direction.SELL)) {
          message =
              String.format(
                  "%s order in reserve on %s %s/%s market has amount %s, "
                      + "but available balance is %s. The amount may be reduced "
                      + "when the order is placed to match the current balance.",
                  capitalize(job.direction().toString().toLowerCase()),
                  name(job.tickTrigger().exchange()),
                  job.tickTrigger().base(),
                  job.tickTrigger().counter(),
                  amount.toPlainString(),
                  available.toPlainString());
        } else {
          message =
              String.format(
                  "%s order in reserve on %s %s/%s market has amount %s, "
                      + "but %s balance only sufficient for %s. The amount may be reduced "
                      + "when the order is placed to match the current balance.",
                  capitalize(job.direction().toString().toLowerCase()),
                  name(job.tickTrigger().exchange()),
                  job.tickTrigger().base(),
                  job.tickTrigger().counter(),
                  amount.toPlainString(),
                  job.tickTrigger().counter(),
                  available.toPlainString());
        }
        notificationService.alert(message);
        jobControl.replace(job.toBuilder().balanceState(INSUFFICIENT_BALANCE).build());
      }
    }
  }

  private void setupOrder(Consumer<String> onAlert) {
    BigDecimal amount = calculator.adjustAmountForLotSize(job.amount());
    if (amount.compareTo(job.amount()) != 0) {
      onAlert.accept(
          "Reduced order size of "
              + amount
              + " to "
              + amount
              + " to conform to exchange amount step size");
    }
    this.order =
        new LimitOrder(
            job.direction() == Direction.SELL ? Order.OrderType.ASK : Order.OrderType.BID,
            amount,
            job.tickTrigger().currencyPair(),
            null,
            new Date(),
            job.limitPrice());
    this.order.addOrderFlag(
        new BinanceTradeService.BinanceOrderFlags() {
          @Override
          public String getClientId() {
            return job.id();
          }
        });
  }

  /**
   * For most exchanges, we have no idempotence protection, so all we do here is to prepare the
   * order, actually performing the order in {@link #stop()} where it won't get retried (accepting
   * that this means we simply have to give up, which isn't ideal).
   *
   * <p>For Binance, though, we can be sure that the exchange won't let us double-submit an order,
   * by using a client order id, so we do it here where retries are allowed.
   */
  @Override
  public Status start() {
    // Place the order (or prepare to place it in the stop handler for non-idempotent
    // exchanges).
    setupOrder(notificationService::alert);
    tradeService = tradeServiceFactory.getForExchange(job.tickTrigger().exchange());
    if (binance()) {
      return binancePlaceOrder(true);
    } else {
      return SUCCESS;
    }
  }

  @Override
  public void setReplacedJob(LimitOrderJob job) {
    this.job = job;
  }

  /**
   * For other exchanges, we do the actual trade in the stop handler to make absolutely sure that
   * the code is never retried.
   */
  @Override
  public void stop() {
    if (binance()) return;
    nonBinancePlaceOrder(true);
  }

  private Status binancePlaceOrder(boolean allowSecondAttempt) {
    String xChangeOrderId = null;
    try {
      rateController.acquire();
      xChangeOrderId = BinanceExceptionClassifier.call(() -> tradeService.placeLimitOrder(order));
    } catch (FundsExceededException e) {
      if (allowSecondAttempt) {
        updateOrderToMatchBalance();
        if (order.getOriginalAmount().compareTo(minimumAmount) < 0) {
          reportFailed(
              job,
              new FundsExceededException("Balance is below minimum order size"),
              FAILURE_PERMANENT);
          return Status.FAILURE_PERMANENT;
        }
        return binancePlaceOrder(false);
      } else {
        reportFailed(job, e, FAILURE_PERMANENT);
        return Status.FAILURE_PERMANENT;
      }
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

  private void updateOrderToMatchBalance() {
    BigDecimal validSaleAmount =
        calculator.validOrderAmount(
            job.limitPrice(),
            job.direction().equals(Direction.SELL) ? OrderType.ASK : OrderType.BID);
    if (validSaleAmount.compareTo(ZERO) < 0) {
      validSaleAmount = ZERO;
    }
    String message =
        String.format(
            "Reducing amount on %s order on %s %s/%s market from %s to %s to fit available balance and step size.",
            job.direction().toString().toLowerCase(),
            name(job.tickTrigger().exchange()),
            job.tickTrigger().base(),
            job.tickTrigger().counter(),
            stripZeros(job.amount()).toPlainString(),
            stripZeros(validSaleAmount).toPlainString());
    notificationService.alert(message);
    order = LimitOrder.Builder.from(order).originalAmount(validSaleAmount).build();
  }

  private void nonBinancePlaceOrder(boolean allowSecondAttempt) {
    String xChangeOrderId;
    try {
      rateController.acquire();
      xChangeOrderId = tradeService.placeLimitOrder(order);
    } catch (FundsExceededException e) {
      if (allowSecondAttempt) {
        updateOrderToMatchBalance();
        if (order.getOriginalAmount().compareTo(minimumAmount) < 0) {
          reportFailed(
              job,
              new FundsExceededException("Balance is below minimum order size"),
              FAILURE_PERMANENT);
          return;
        }
        nonBinancePlaceOrder(false);
        return;
      } else {
        reportFailed(job, e, FAILURE_PERMANENT);
        return;
      }
    } catch (Exception e) {
      reportFailed(job, e, FAILURE_PERMANENT);
      return;
    }
    reportSuccess(job, xChangeOrderId);
  }

  private void reportDuplicate(final LimitOrderJob job) {
    String message =
        String.format(
            "Duplicate order on %s %s/%s market filtered by exchange: %s %s at %s",
            job.tickTrigger().exchange(),
            job.tickTrigger().base(),
            job.tickTrigger().counter(),
            job.direction().toString().toLowerCase(),
            stripZeros(job.amount()).toPlainString(),
            job.limitPrice().toPlainString());
    LOGGER.warn(message);
    notificationService.alert(message);
  }

  private void reportSuccess(final LimitOrderJob job, String xChangeOrderId) {
    String message =
        String.format(
            "Order %s placed on %s %s/%s market: %s %s at %s",
            xChangeOrderId,
            job.tickTrigger().exchange(),
            job.tickTrigger().base(),
            job.tickTrigger().counter(),
            job.direction().toString().toLowerCase(),
            stripZeros(order.getOriginalAmount()).toPlainString(),
            job.limitPrice().toPlainString());
    notificationService.alert(message);
  }

  private void reportFailed(final LimitOrderJob job, Exception e, Status failureStatus) {
    String message =
        String.format(
            "Error placing order on %s %s/%s market: %s %s at %s (%s)",
            job.tickTrigger().exchange(),
            job.tickTrigger().base(),
            job.tickTrigger().counter(),
            job.direction().toString().toLowerCase(),
            stripZeros(order.getOriginalAmount()).toPlainString(),
            job.limitPrice().toPlainString(),
            e.getMessage());
    statusUpdateService.status(job.id(), failureStatus);
    if (e instanceof FundsExceededException) {
      // Don't stack trace for this sort of expected error.
      notificationService.error(message);
    } else {
      notificationService.error(message, e);
    }
  }

  private boolean binance() {
    return job.tickTrigger().exchange().equals(Exchanges.BINANCE);
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(
          new FactoryModuleBuilder()
              .implement(LimitOrderJob.Processor.class, LimitOrderJobProcessor.class)
              .build(LimitOrderJob.Processor.ProcessorFactory.class));
    }
  }
}
