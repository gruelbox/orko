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

import static com.gruelbox.orko.exchange.Exchanges.BINANCE;
import static com.gruelbox.orko.exchange.MarketDataType.BALANCE;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.gruelbox.orko.exchange.BalanceEvent;
import com.gruelbox.orko.exchange.ExchangeEventRegistry;
import com.gruelbox.orko.exchange.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.exchange.MarketDataSubscription;
import com.gruelbox.orko.exchange.MaxTradeAmountCalculator;
import com.gruelbox.orko.exchange.RateController;
import com.gruelbox.orko.exchange.TradeServiceFactory;
import com.gruelbox.orko.job.LimitOrderJob.BalanceState;
import com.gruelbox.orko.job.LimitOrderJob.Direction;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.jobrun.spi.StatusUpdateService;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;
import io.reactivex.Flowable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.AssertionFailedError;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.binance.dto.BinanceException;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.FundsExceededException;
import org.knowm.xchange.service.trade.TradeService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLimitOrderJobProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestLimitOrderJobProcessor.class);

  private static final BigDecimal AMOUNT = new BigDecimal(1000);
  private static final BigDecimal PRICE = new BigDecimal(95);
  private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
  private static final int PRICE_SCALE = 3;

  private static final String BASE = "FOO";
  private static final String COUNTER = "USDT";
  private static final CurrencyPair CURRENCY_PAIR = new CurrencyPair(BASE, COUNTER);
  private static final String EXCHANGE = "fooex";

  @Mock private StatusUpdateService statusUpdateService;
  @Mock private NotificationService notificationService;
  @Mock private ExchangeEventRegistry exchangeEventRegistry;
  @Mock private ExchangeService exchangeService;

  @Mock private Exchange exchange;
  @Mock private ExchangeMetaData exchangeMetaData;
  @Mock private CurrencyPairMetaData currencyPairMetaData;
  @Mock private TradeServiceFactory tradeServiceFactory;
  @Mock private TradeService tradeService;
  @Mock private JobControl jobControl;
  @Mock private RateController rateController;

  private final AtomicInteger xChangeOrderId = new AtomicInteger();

  @Before
  public void before() throws IOException {

    MockitoAnnotations.initMocks(this);

    when(tradeServiceFactory.getForExchange(EXCHANGE)).thenReturn(tradeService);
    when(tradeServiceFactory.getForExchange(Exchanges.BINANCE)).thenReturn(tradeService);
    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class)))
        .thenAnswer(args -> newTradeId());
    when(exchangeService.rateController(EXCHANGE)).thenReturn(rateController);
    when(exchangeService.rateController(BINANCE)).thenReturn(rateController);
    when(exchangeService.get(EXCHANGE)).thenReturn(exchange);
    when(exchangeService.get(BINANCE)).thenReturn(exchange);
    when(exchange.getExchangeMetaData()).thenReturn(exchangeMetaData);
    when(exchangeMetaData.getCurrencyPairs())
        .thenReturn(ImmutableMap.of(CURRENCY_PAIR, currencyPairMetaData));
    when(currencyPairMetaData.getPriceScale()).thenReturn(PRICE_SCALE);
    when(currencyPairMetaData.getMinimumAmount()).thenReturn(MIN_AMOUNT);
    when(currencyPairMetaData.getMinimumAmount()).thenReturn(MIN_AMOUNT);

    Mockito.doAnswer(
            inv -> {
              LOGGER.info(inv.getArgument(0, String.class));
              return null;
            })
        .when(notificationService)
        .alert(Mockito.anyString());
    Mockito.doAnswer(
            inv -> {
              LOGGER.error(inv.getArgument(0, String.class));
              return null;
            })
        .when(notificationService)
        .error(Mockito.anyString());
    Mockito.doAnswer(
            inv -> {
              LOGGER.error(inv.getArgument(0, String.class), inv.getArgument(1, Exception.class));
              return null;
            })
        .when(notificationService)
        .error(Mockito.anyString(), Mockito.any(Exception.class));
  }

  @Test
  public void testSell() throws Exception {
    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(MIN_AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.SELL)
            .build();

    LimitOrderJobProcessor processor = newProcessor(job);

    balanceAvailable(ex, BASE, MIN_AMOUNT);
    processor.validate();

    Status result = processor.start();
    processor.stop();

    verifyLimitSell(MIN_AMOUNT);
    verifySentMessage();
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testSellLotSizeReduction() throws Exception {
    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(new BigDecimal("0.018"))
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.SELL)
            .build();

    when(currencyPairMetaData.getAmountStepSize()).thenReturn(new BigDecimal("0.005"));

    LimitOrderJobProcessor processor = newProcessor(job);

    balanceAvailable(ex, BASE, new BigDecimal("0.018"));

    Status result = processor.start();
    processor.stop();

    verifyLimitSell(new BigDecimal("0.015"));
    verifySentMessage(2);
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testBuy() throws Exception {
    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(MIN_AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.BUY)
            .build();

    LimitOrderJobProcessor processor = newProcessor(job);

    balanceAvailable(ex, COUNTER, MIN_AMOUNT.multiply(PRICE));
    processor.validate();

    Status result = processor.start();
    processor.stop();

    verifyLimitBuy(MIN_AMOUNT);
    verifySentMessage();
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testSellAlertBalanceLow() throws Exception {
    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.SELL)
            .build();

    LimitOrderJobProcessor processor = newProcessor(job);

    balanceAvailable(ex, BASE, AMOUNT.subtract(new BigDecimal("0.001")));
    processor.validate();

    verify(notificationService).alert(Mockito.anyString());
    verify(jobControl)
        .replace(
            LimitOrderJob.builder()
                .amount(AMOUNT)
                .limitPrice(PRICE)
                .tickTrigger(ex)
                .direction(Direction.SELL)
                .balanceState(BalanceState.INSUFFICIENT_BALANCE)
                .build());
  }

  @Test
  public void testSellAlertBalanceBackUp() throws Exception {
    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.SELL)
            .balanceState(BalanceState.INSUFFICIENT_BALANCE)
            .build();

    LimitOrderJobProcessor processor = newProcessor(job);

    balanceAvailable(ex, BASE, AMOUNT);
    processor.validate();

    verify(notificationService).alert(Mockito.anyString());
    verify(jobControl)
        .replace(
            LimitOrderJob.builder()
                .amount(AMOUNT)
                .limitPrice(PRICE)
                .tickTrigger(ex)
                .direction(Direction.SELL)
                .balanceState(BalanceState.SUFFICIENT_BALANCE)
                .build());
  }

  @Test
  public void testBuyAlertBalanceLow() throws Exception {
    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.BUY)
            .build();

    LimitOrderJobProcessor processor = newProcessor(job);

    balanceAvailable(ex, COUNTER, AMOUNT.multiply(PRICE).subtract(new BigDecimal("0.001")));
    processor.validate();

    verify(notificationService).alert(Mockito.anyString());
    verify(jobControl)
        .replace(
            LimitOrderJob.builder()
                .amount(AMOUNT)
                .limitPrice(PRICE)
                .tickTrigger(ex)
                .direction(Direction.BUY)
                .balanceState(BalanceState.INSUFFICIENT_BALANCE)
                .build());
  }

  @Test
  public void testBuyAlertBalanceBackUp() throws Exception {
    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.BUY)
            .balanceState(BalanceState.INSUFFICIENT_BALANCE)
            .build();

    LimitOrderJobProcessor processor = newProcessor(job);

    balanceAvailable(ex, COUNTER, AMOUNT.multiply(PRICE));
    processor.validate();

    verify(notificationService).alert(Mockito.anyString());
    verify(jobControl)
        .replace(
            LimitOrderJob.builder()
                .amount(AMOUNT)
                .limitPrice(PRICE)
                .tickTrigger(ex)
                .direction(Direction.BUY)
                .balanceState(BalanceState.SUFFICIENT_BALANCE)
                .build());
  }

  @Test
  public void testBuyAlertBalanceLowAlreadyAlerted() throws Exception {
    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.BUY)
            .balanceState(BalanceState.INSUFFICIENT_BALANCE)
            .build();

    LimitOrderJobProcessor processor = newProcessor(job);

    balanceAvailable(ex, COUNTER, AMOUNT.multiply(PRICE).subtract(new BigDecimal("0.001")));
    processor.validate();

    verifyNoMoreInteractions(notificationService, jobControl);
  }

  @Test
  public void testSellAlertBalanceLowAlreadyAlerted() throws Exception {
    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.SELL)
            .balanceState(BalanceState.INSUFFICIENT_BALANCE)
            .build();

    LimitOrderJobProcessor processor = newProcessor(job);

    balanceAvailable(ex, BASE, AMOUNT.subtract(new BigDecimal("0.001")));
    processor.validate();

    verifyNoMoreInteractions(notificationService, jobControl);
  }

  @Test
  public void testBuyNoTrack() throws Exception {
    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.BUY)
            .build();

    LimitOrderJobProcessor processor = newProcessor(job);
    Status result = processor.start();
    processor.stop();

    verifyLimitBuy();
    verifySentMessage();
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testSellFailed() throws Exception {
    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.SELL)
            .build();

    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class)))
        .thenThrow(new RuntimeException());

    LimitOrderJobProcessor processor = newProcessor(job);
    Status result = processor.start();
    processor.stop();

    verifyLimitSell();
    verifySentError();
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testBuyFailed() throws Exception {
    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.BUY)
            .build();

    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class)))
        .thenThrow(new RuntimeException());

    LimitOrderJobProcessor processor = newProcessor(job);
    Status result = processor.start();
    processor.stop();

    verifyLimitBuy();
    verifySentError();
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testBuyFailedFundsExceeded() throws Exception {

    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.BUY)
            .build();

    // Even on a second attempt, return insufficient balance
    balanceAvailable(ex, COUNTER, MIN_AMOUNT.multiply(PRICE).subtract(new BigDecimal("0.001")));

    orderSuccessOnSecondAttempt();

    LimitOrderJobProcessor processor = newProcessor(job);
    Status result = processor.start();
    processor.stop();

    verifyLimitBuy();
    verifySentMinorError();
    verifySentMessage();
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testBuyFailedFundsExceededButCanReduce() throws Exception {

    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.BUY)
            .build();

    // On second attempt, return the minimum balance
    balanceAvailable(ex, COUNTER, MIN_AMOUNT.multiply(PRICE));

    orderSuccessOnSecondAttempt();

    LimitOrderJobProcessor processor = newProcessor(job);
    Status result = processor.start();
    processor.stop();

    verify(tradeService, times(2)).placeLimitOrder(Mockito.any(LimitOrder.class));
    verifySentMessage(2);
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testBuyFailedFundsExceededButCanReduceWithScaling() throws Exception {

    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.BUY)
            .build();

    // On second attempt, return the minimum balance plus a tiny amount which
    // should be rounded down.
    balanceAvailable(ex, COUNTER, MIN_AMOUNT.multiply(PRICE).add(new BigDecimal("0.0001")));

    orderSuccessOnSecondAttempt();

    LimitOrderJobProcessor processor = newProcessor(job);
    Status result = processor.start();
    processor.stop();

    ArgumentCaptor<LimitOrder> captor = ArgumentCaptor.forClass(LimitOrder.class);
    verify(tradeService, times(2)).placeLimitOrder(captor.capture());
    Assert.assertEquals(PRICE, captor.getAllValues().get(0).getLimitPrice());
    Assert.assertEquals(AMOUNT, captor.getAllValues().get(0).getOriginalAmount());
    Assert.assertEquals(CURRENCY_PAIR, captor.getAllValues().get(0).getCurrencyPair());
    Assert.assertEquals(Order.OrderType.BID, captor.getAllValues().get(0).getType());
    Assert.assertEquals(PRICE, captor.getAllValues().get(1).getLimitPrice());
    Assert.assertEquals(
        MIN_AMOUNT.stripTrailingZeros(),
        captor.getAllValues().get(1).getOriginalAmount().stripTrailingZeros());
    Assert.assertEquals(CURRENCY_PAIR, captor.getAllValues().get(1).getCurrencyPair());
    Assert.assertEquals(Order.OrderType.BID, captor.getAllValues().get(1).getType());

    verifySentMessage(2);
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testBuyFailedFundsExceededBinance() throws Exception {

    TickerSpec ex =
        TickerSpec.builder().base(BASE).counter(COUNTER).exchange(Exchanges.BINANCE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.BUY)
            .build();

    // Even on a second attempt, return insufficient balance
    balanceAvailable(ex, COUNTER, MIN_AMOUNT.multiply(PRICE).subtract(new BigDecimal("0.001")));

    orderSuccessOnSecondAttempt();

    LimitOrderJobProcessor processor = newProcessor(job);
    processor.start();

    verifyLimitBuy();
    verifySentMinorError();
    verifySentMessage();
    verifyDidNothingElse();
  }

  @Test
  public void testSellFailedFundsExceeded() throws Exception {

    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.SELL)
            .build();

    // Even on a second attempt, return insufficient balance
    balanceAvailable(ex, BASE, MIN_AMOUNT.subtract(new BigDecimal("0.0001")));

    orderSuccessOnSecondAttempt();

    LimitOrderJobProcessor processor = newProcessor(job);
    Status result = processor.start();
    processor.stop();

    verifyLimitSell();
    verifySentMinorError();
    verifySentMessage();
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testSellFailedFundsExceededBinance() throws Exception {

    TickerSpec ex =
        TickerSpec.builder().base(BASE).counter(COUNTER).exchange(Exchanges.BINANCE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.SELL)
            .build();

    // Even on a second attempt, return insufficient balance
    balanceAvailable(ex, BASE, MIN_AMOUNT.subtract(new BigDecimal("0.0001")));

    orderSuccessOnSecondAttempt();

    LimitOrderJobProcessor processor = newProcessor(job);
    processor.start();

    verifyLimitSell();
    verifySentMinorError();
    verifySentMessage();
    verifyDidNothingElse();
  }

  @Test
  public void testSellFailedFundsExceededButCanReduce() throws Exception {

    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.SELL)
            .build();

    // On second attempt, return the minimum balance
    balanceAvailable(ex, BASE, MIN_AMOUNT);

    orderSuccessOnSecondAttempt();

    LimitOrderJobProcessor processor = newProcessor(job);
    Status result = processor.start();
    processor.stop();

    verify(tradeService, times(2)).placeLimitOrder(Mockito.any(LimitOrder.class));
    verifySentMessage(2);
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testSellFailedFundsExceededButCanReduceWithScaling() throws Exception {

    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.SELL)
            .build();

    // On second attempt, return the minimum balance plus a roundable amount
    balanceAvailable(ex, BASE, MIN_AMOUNT.add(new BigDecimal("0.0001")));

    orderSuccessOnSecondAttempt();

    LimitOrderJobProcessor processor = newProcessor(job);
    Status result = processor.start();
    processor.stop();

    ArgumentCaptor<LimitOrder> captor = ArgumentCaptor.forClass(LimitOrder.class);
    verify(tradeService, times(2)).placeLimitOrder(captor.capture());
    Assert.assertEquals(PRICE, captor.getAllValues().get(0).getLimitPrice());
    Assert.assertEquals(AMOUNT, captor.getAllValues().get(0).getOriginalAmount());
    Assert.assertEquals(CURRENCY_PAIR, captor.getAllValues().get(0).getCurrencyPair());
    Assert.assertEquals(Order.OrderType.ASK, captor.getAllValues().get(0).getType());
    Assert.assertEquals(PRICE, captor.getAllValues().get(1).getLimitPrice());
    Assert.assertEquals(
        MIN_AMOUNT.stripTrailingZeros(),
        captor.getAllValues().get(1).getOriginalAmount().stripTrailingZeros());
    Assert.assertEquals(CURRENCY_PAIR, captor.getAllValues().get(1).getCurrencyPair());
    Assert.assertEquals(Order.OrderType.ASK, captor.getAllValues().get(1).getType());

    verifySentMessage(2);
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testSellFailedFundsExceededButCanReduceWithLotSize() throws Exception {

    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.SELL)
            .build();

    when(currencyPairMetaData.getAmountStepSize()).thenReturn(new BigDecimal(10));

    // On second attempt, return an amount we must round down to
    // hit the lot size.
    balanceAvailable(ex, BASE, new BigDecimal("39.9"));

    orderSuccessOnSecondAttempt();

    LimitOrderJobProcessor processor = newProcessor(job);
    Status result = processor.start();
    processor.stop();

    ArgumentCaptor<LimitOrder> captor = ArgumentCaptor.forClass(LimitOrder.class);
    verify(tradeService, times(2)).placeLimitOrder(captor.capture());
    Assert.assertEquals(PRICE, captor.getAllValues().get(0).getLimitPrice());
    Assert.assertEquals(AMOUNT, captor.getAllValues().get(0).getOriginalAmount());
    Assert.assertEquals(CURRENCY_PAIR, captor.getAllValues().get(0).getCurrencyPair());
    Assert.assertEquals(Order.OrderType.ASK, captor.getAllValues().get(0).getType());
    Assert.assertEquals(PRICE, captor.getAllValues().get(1).getLimitPrice());
    Assert.assertEquals(
        new BigDecimal("30").stripTrailingZeros(),
        captor.getAllValues().get(1).getOriginalAmount().stripTrailingZeros());
    Assert.assertEquals(CURRENCY_PAIR, captor.getAllValues().get(1).getCurrencyPair());
    Assert.assertEquals(Order.OrderType.ASK, captor.getAllValues().get(1).getType());

    verifySentMessage(2);
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testBuyFailedBinance() throws Exception {
    TickerSpec ex =
        TickerSpec.builder().base(BASE).counter(COUNTER).exchange(Exchanges.BINANCE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.BUY)
            .build();

    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class)))
        .thenThrow(new RuntimeException());

    LimitOrderJobProcessor processor = newProcessor(job);
    Status result = processor.start();
    processor.stop();

    verifyLimitBuy();
    verifySentError();
    Assert.assertEquals(Status.FAILURE_PERMANENT, result);
    verifyDidNothingElse();
  }

  @Test
  public void testBuyFailedBinancePermanent() throws Exception {
    TickerSpec ex =
        TickerSpec.builder().base(BASE).counter(COUNTER).exchange(Exchanges.BINANCE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.BUY)
            .build();

    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class)))
        .thenThrow(new BinanceException(-1100, "Foo"));

    LimitOrderJobProcessor processor = newProcessor(job);
    Status result = processor.start();
    processor.stop();

    verifyLimitBuy();
    verifySentError();
    Assert.assertEquals(Status.FAILURE_PERMANENT, result);
    verifyDidNothingElse();
  }

  @Test
  public void testBuyFailedBinanceTransient() throws Exception {
    TickerSpec ex =
        TickerSpec.builder().base(BASE).counter(COUNTER).exchange(Exchanges.BINANCE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.BUY)
            .build();

    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class)))
        .thenThrow(new BinanceException(-1000, "Foo"));

    LimitOrderJobProcessor processor = newProcessor(job);
    Status result = processor.start();
    processor.stop();

    verifyLimitBuy();
    verifySentTransientError();
    Assert.assertEquals(Status.FAILURE_TRANSIENT, result);
    verifyDidNothingElse();
  }

  @Test
  public void testBuyFailedBinanceDuplicate() throws Exception {
    TickerSpec ex =
        TickerSpec.builder().base(BASE).counter(COUNTER).exchange(Exchanges.BINANCE).build();
    LimitOrderJob job =
        LimitOrderJob.builder()
            .amount(AMOUNT)
            .limitPrice(PRICE)
            .tickTrigger(ex)
            .direction(Direction.BUY)
            .build();

    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class)))
        .thenThrow(new BinanceException(-2010, "Foo"));

    LimitOrderJobProcessor processor = newProcessor(job);
    Status result = processor.start();
    processor.stop();

    verifyLimitBuy();
    verifySentMessage();
    Assert.assertEquals(Status.SUCCESS, result);
    verifyDidNothingElse();
  }

  /* ---------------------------------- Utility methods  ---------------------------------------------------- */

  private LimitOrderJobProcessor newProcessor(LimitOrderJob job) {
    return new LimitOrderJobProcessor(
        job,
        jobControl,
        statusUpdateService,
        notificationService,
        tradeServiceFactory,
        exchangeEventRegistry,
        exchangeService,
        new MaxTradeAmountCalculator.Factory(exchangeEventRegistry, exchangeService));
  }

  private void balanceAvailable(TickerSpec ex, String currency, BigDecimal amount) {
    ExchangeEventSubscription subscription = Mockito.mock(ExchangeEventSubscription.class);
    when(exchangeEventRegistry.subscribe(MarketDataSubscription.create(ex, BALANCE)))
        .thenReturn(subscription);
    when(subscription.getBalances())
        .thenReturn(
            Flowable.just(
                BalanceEvent.create(
                    EXCHANGE,
                    new org.knowm.xchange.dto.account.Balance(
                        Currency.getInstance(currency), amount))));
  }

  private void orderSuccessOnSecondAttempt() throws IOException {
    AtomicInteger attempt = new AtomicInteger(0);
    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class)))
        .thenAnswer(
            inv -> {
              if (attempt.getAndIncrement() == 0) {
                throw new FundsExceededException("Not enough cash");
              } else if (inv.getArgument(0, LimitOrder.class)
                      .getOriginalAmount()
                      .stripTrailingZeros()
                      .scale()
                  > PRICE_SCALE) {
                throw new AssertionFailedError("Invalid scale on trade attempt");
              } else {
                return "1";
              }
            });
  }

  private void verifyDidNothingElse() {
    verifyNoMoreInteractions(notificationService, tradeService, jobControl);
  }

  private void verifySentError() {
    verify(notificationService).error(Mockito.anyString(), Mockito.any(RuntimeException.class));
  }

  private void verifySentMinorError() {
    verify(notificationService).error(Mockito.anyString());
  }

  private void verifySentTransientError() {
    verify(notificationService)
        .error(
            Mockito.anyString(),
            Mockito.any(BinanceExceptionClassifier.RetriableBinanceException.class));
  }

  private void verifySentMessage() {
    verify(notificationService).alert(Mockito.anyString());
  }

  private void verifySentMessage(int times) {
    verify(notificationService, times(times)).alert(Mockito.anyString());
  }

  private void verifyLimitSell() throws IOException {
    verifyLimitSell(AMOUNT);
  }

  private void verifyLimitSell(BigDecimal amount) throws IOException {
    ArgumentCaptor<LimitOrder> captor = ArgumentCaptor.forClass(LimitOrder.class);
    verify(tradeService).placeLimitOrder(captor.capture());
    Assert.assertEquals(PRICE, captor.getValue().getLimitPrice());
    Assert.assertEquals(amount, captor.getValue().getOriginalAmount());
    Assert.assertEquals(CURRENCY_PAIR, captor.getValue().getCurrencyPair());
    Assert.assertEquals(Order.OrderType.ASK, captor.getValue().getType());
  }

  private void verifyLimitBuy() throws IOException {
    verifyLimitBuy(AMOUNT);
  }

  private void verifyLimitBuy(BigDecimal amount) throws IOException {
    ArgumentCaptor<LimitOrder> captor = ArgumentCaptor.forClass(LimitOrder.class);
    verify(tradeService).placeLimitOrder(captor.capture());
    Assert.assertEquals(PRICE, captor.getValue().getLimitPrice());
    Assert.assertEquals(amount, captor.getValue().getOriginalAmount());
    Assert.assertEquals(CURRENCY_PAIR, captor.getValue().getCurrencyPair());
    Assert.assertEquals(Order.OrderType.BID, captor.getValue().getType());
  }

  private void verifyFinished(Status status) {
    Assert.assertEquals(Status.SUCCESS, status);
  }

  private String newTradeId() {
    return Integer.toString(xChangeOrderId.incrementAndGet());
  }
}
