package com.grahamcrockford.oco.core.jobs;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOError;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.trade.TradeService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.grahamcrockford.oco.api.exchange.ExchangeService;
import com.grahamcrockford.oco.api.exchange.TradeServiceFactory;
import com.grahamcrockford.oco.api.job.LimitOrderJob;
import com.grahamcrockford.oco.api.job.OrderStateNotifier;
import com.grahamcrockford.oco.api.job.LimitOrderJob.Direction;
import com.grahamcrockford.oco.api.process.JobSubmitter;
import com.grahamcrockford.oco.core.telegram.TelegramService;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.TickerSpec;

public class TestLimitOrderJobProcessor {

  private static final BigDecimal AMOUNT = new BigDecimal(1000);
  private static final BigDecimal PRICE = new BigDecimal(95);

  private static final String BASE = "FOO";
  private static final String COUNTER = "USDT";
  private static final CurrencyPair CURRENCY_PAIR = new CurrencyPair(BASE, COUNTER);
  private static final String EXCHANGE = "fooex";

  @Mock private JobSubmitter enqueuer;
  @Mock private TelegramService telegramService;
  @Mock private ExchangeService exchangeService;

  @Mock private Exchange exchange;
  @Mock private TradeServiceFactory tradeServiceFactory;
  @Mock private TradeService tradeService;
  @Mock private JobControl jobControl;

  private final AtomicInteger xChangeOrderId = new AtomicInteger();

  @Before
  public void before() throws IOException {

    MockitoAnnotations.initMocks(this);

    when(tradeServiceFactory.getForExchange(EXCHANGE)).thenReturn(tradeService);
    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class))).thenAnswer(args -> newTradeId());
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testSell() throws Exception {
    TickerSpec ex = TickerSpec.builder()
        .base(BASE)
        .counter(COUNTER)
        .exchange(EXCHANGE)
        .build();
    LimitOrderJob job = LimitOrderJob.builder()
        .amount(AMOUNT)
        .limitPrice(PRICE)
        .tickTrigger(ex)
        .direction(Direction.SELL)
        .build();

    LimitOrderJobProcessor processor = new LimitOrderJobProcessor(job, jobControl, telegramService, tradeServiceFactory, enqueuer);
    boolean result = processor.start();
    processor.stop();

    verifyLimitSell();
    verifySubmitWatcher();
    verifySentMessage();
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testBuy() throws Exception {
    TickerSpec ex = TickerSpec.builder()
        .base(BASE)
        .counter(COUNTER)
        .exchange(EXCHANGE)
        .build();
    LimitOrderJob job = LimitOrderJob.builder()
        .amount(AMOUNT)
        .limitPrice(PRICE)
        .tickTrigger(ex)
        .direction(Direction.BUY)
        .build();

    LimitOrderJobProcessor processor = new LimitOrderJobProcessor(job, jobControl, telegramService, tradeServiceFactory, enqueuer);
    boolean result = processor.start();
    processor.stop();

    verifyLimitBuy();
    verifySubmitWatcher();
    verifySentMessage();
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSellFailed() throws Exception {
    TickerSpec ex = TickerSpec.builder()
        .base(BASE)
        .counter(COUNTER)
        .exchange(EXCHANGE)
        .build();
    LimitOrderJob job = LimitOrderJob.builder()
        .amount(AMOUNT)
        .limitPrice(PRICE)
        .tickTrigger(ex)
        .direction(Direction.SELL)
        .build();

    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class))).thenThrow(IOError.class);

    LimitOrderJobProcessor processor = new LimitOrderJobProcessor(job, jobControl, telegramService, tradeServiceFactory, enqueuer);
    boolean result = processor.start();
    processor.stop();

    verifyLimitSell();
    verifySentMessage();
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testBuyFailed() throws Exception {
    TickerSpec ex = TickerSpec.builder()
        .base(BASE)
        .counter(COUNTER)
        .exchange(EXCHANGE)
        .build();
    LimitOrderJob job = LimitOrderJob.builder()
        .amount(AMOUNT)
        .limitPrice(PRICE)
        .tickTrigger(ex)
        .direction(Direction.BUY)
        .build();

    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class))).thenThrow(IOError.class);

    LimitOrderJobProcessor processor = new LimitOrderJobProcessor(job, jobControl, telegramService, tradeServiceFactory, enqueuer);
    boolean result = processor.start();
    processor.stop();

    verifyLimitBuy();
    verifySentMessage();
    verifyFinished(result);
    verifyDidNothingElse();
  }


  /* ---------------------------------- Utility methods  ---------------------------------------------------- */

  private void verifyDidNothingElse() {
    verifyNoMoreInteractions(telegramService, tradeService, enqueuer, jobControl);
  }

  private void verifySentMessage() {
    verify(telegramService).sendMessage(Mockito.anyString());
  }

  private void verifyLimitSell() throws IOException {
    ArgumentCaptor<LimitOrder> captor = ArgumentCaptor.forClass(LimitOrder.class);
    verify(tradeService).placeLimitOrder(captor.capture());
    Assert.assertEquals(PRICE, captor.getValue().getLimitPrice());
    Assert.assertEquals(AMOUNT, captor.getValue().getOriginalAmount());
    Assert.assertEquals(CURRENCY_PAIR, captor.getValue().getCurrencyPair());
    Assert.assertEquals(Order.OrderType.ASK, captor.getValue().getType());
  }

  private void verifyLimitBuy() throws IOException {
    ArgumentCaptor<LimitOrder> captor = ArgumentCaptor.forClass(LimitOrder.class);
    verify(tradeService).placeLimitOrder(captor.capture());
    Assert.assertEquals(PRICE, captor.getValue().getLimitPrice());
    Assert.assertEquals(AMOUNT, captor.getValue().getOriginalAmount());
    Assert.assertEquals(CURRENCY_PAIR, captor.getValue().getCurrencyPair());
    Assert.assertEquals(Order.OrderType.BID, captor.getValue().getType());
  }

  private void verifySubmitWatcher() throws Exception {
    verify(enqueuer).submitNew(OrderStateNotifier.builder()
        .description("Stop")
        .orderId(lastTradeId())
        .exchange(EXCHANGE)
        .build());
  }

  private void verifyFinished(boolean result) {
    Assert.assertFalse(result);
  }

  private String newTradeId() {
    return Integer.toString(xChangeOrderId.incrementAndGet());
  }

  private String lastTradeId() {
    return Integer.toString(xChangeOrderId.get());
  }
}