package com.grahamcrockford.oco.core.jobs;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOError;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.trade.TradeService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.grahamcrockford.oco.api.TickerSpec;
import com.grahamcrockford.oco.core.ExchangeService;
import com.grahamcrockford.oco.core.JobSubmitter;
import com.grahamcrockford.oco.core.TradeServiceFactory;
import com.grahamcrockford.oco.core.jobs.OrderStateNotifier;
import com.grahamcrockford.oco.telegram.TelegramService;

public class TestLimitSellProcessor {

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

  private LimitSellProcessor processor;
  private final AtomicInteger xChangeOrderId = new AtomicInteger();

  @Before
  public void before() throws IOException {

    MockitoAnnotations.initMocks(this);

    when(tradeServiceFactory.getForExchange(EXCHANGE)).thenReturn(tradeService);
    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class))).thenAnswer(args -> newTradeId());

    processor = new LimitSellProcessor(telegramService, tradeServiceFactory, enqueuer);
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testSell() throws Exception {
    TickerSpec ex = TickerSpec.builder()
        .base(BASE)
        .counter(COUNTER)
        .exchange(EXCHANGE)
        .build();
    LimitSell job = LimitSell.builder()
        .amount(AMOUNT)
        .limitPrice(PRICE)
        .tickTrigger(ex)
        .build();

    Optional<LimitSell> result = processor.process(job);

    verifyLimitSell();
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
    LimitSell job = LimitSell.builder()
        .amount(AMOUNT)
        .limitPrice(PRICE)
        .tickTrigger(ex)
        .build();

    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class))).thenThrow(IOError.class);

    Optional<LimitSell> result = processor.process(job);

    verifyLimitSell();
    verifySentMessage();
    verifyFinished(result);
    verifyDidNothingElse();
  }


  /* ---------------------------------- Utility methods  ---------------------------------------------------- */

  private void verifyDidNothingElse() {
    verifyNoMoreInteractions(telegramService, tradeService, enqueuer);
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
  }

  private void verifySubmitWatcher() {
    verify(enqueuer).submitNew(OrderStateNotifier.builder()
        .description("Stop")
        .orderId(lastTradeId())
        .exchange(EXCHANGE)
        .build());
  }

  private void verifyFinished(Optional<LimitSell> result) {
    Assert.assertFalse(result.isPresent());
  }

  private String newTradeId() {
    return Integer.toString(xChangeOrderId.incrementAndGet());
  }

  private String lastTradeId() {
    return Integer.toString(xChangeOrderId.get());
  }
}