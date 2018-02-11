package com.grahamcrockford.oco.core.advancedorders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.grahamcrockford.oco.api.AdvancedOrderInfo;
import com.grahamcrockford.oco.core.AdvancedOrderEnqueuer;
import com.grahamcrockford.oco.core.AdvancedOrderIdGenerator;
import com.grahamcrockford.oco.core.ExchangeService;
import com.grahamcrockford.oco.core.TelegramService;
import com.grahamcrockford.oco.core.TradeServiceFactory;
import com.grahamcrockford.oco.core.advancedorders.OrderStateNotifier;
import com.grahamcrockford.oco.core.advancedorders.SoftTrailingStop;
import com.grahamcrockford.oco.core.advancedorders.SoftTrailingStopProcessor;

public class TestSoftTrailingStopProcessor {

  private static final int PRICE_SCALE = 2;
  private static final BigDecimal HUNDRED = new BigDecimal(100);
  private static final BigDecimal PENNY = new BigDecimal("0.01");

  private static final long JOB_ID = 555L;

  private static final BigDecimal AMOUNT = new BigDecimal(1000);

  private static final BigDecimal LIMIT_PC = new BigDecimal(10);
  private static final BigDecimal STOP_PC = new BigDecimal(5);

  private static final BigDecimal ENTRY_PRICE = HUNDRED;
  private static final BigDecimal STOP_PRICE = new BigDecimal(95);
  private static final BigDecimal LIMIT_PRICE = new BigDecimal(90);

  private static final BigDecimal HIGHER_ENTRY_PRICE = new BigDecimal(200);
  private static final BigDecimal HIGHER_STOP_PRICE = new BigDecimal(190);

  private static final String BASE = "FOO";
  private static final String COUNTER = "USDT";
  private static final CurrencyPair CURRENCY_PAIR = new CurrencyPair(BASE, COUNTER);
  private static final String EXCHANGE = "fooex";

  @Mock private AdvancedOrderEnqueuer enqueuer;
  @Mock private AdvancedOrderIdGenerator advancedOrderIdGenerator;
  @Mock private TelegramService telegramService;
  @Mock private ExchangeService exchangeService;

  @Mock private Exchange exchange;
  @Mock private MarketDataService marketDataService;
  @Mock private TradeServiceFactory tradeServiceFactory;
  @Mock private TradeService tradeService;
  @Mock private Injector injector;

  private SoftTrailingStopProcessor processor;
  private final AtomicInteger xChangeOrderId = new AtomicInteger();
  private final AtomicLong advancedOrderId = new AtomicLong();

  @Before
  public void before() throws IOException {

    MockitoAnnotations.initMocks(this);

    when(exchangeService.get(EXCHANGE)).thenReturn(exchange);
    when(exchange.getMarketDataService()).thenReturn(marketDataService);
    when(exchange.getTradeService()).thenReturn(tradeService);
    final ExchangeMetaData exchangeMetaData = mock(ExchangeMetaData.class);
    when(exchange.getExchangeMetaData()).thenReturn(exchangeMetaData);
    final CurrencyPairMetaData currencyPairMetaData = mock(CurrencyPairMetaData.class);
    when(exchangeMetaData.getCurrencyPairs()).thenReturn(ImmutableMap.of(CURRENCY_PAIR, currencyPairMetaData));
    when(currencyPairMetaData.getPriceScale()).thenReturn(PRICE_SCALE);

    when(tradeServiceFactory.getForExchange(EXCHANGE)).thenReturn(tradeService);
    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class))).thenAnswer(args -> newTradeId());

    when(advancedOrderIdGenerator.next()).thenAnswer(a -> advancedOrderId.incrementAndGet());

    processor = new SoftTrailingStopProcessor(telegramService, tradeServiceFactory, exchangeService, enqueuer, advancedOrderIdGenerator);
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testErrNoBuyers() throws Exception {
    final Ticker ticker = new Ticker.Builder()
        .last(ENTRY_PRICE)
        .ask(ENTRY_PRICE.add(PENNY))
        .timestamp(new Date())
      .build();

    processor.tick(baseJob().build(), ticker);

    verifySentMessage();
    verifyWillRepeatWithoutChange(baseJob().build());
    verifyDidNothingElse();
  }

  @Test
  public void testErrNoSellers() throws Exception {
    final Ticker ticker = new Ticker.Builder()
        .bid(ENTRY_PRICE.subtract(PENNY))
        .last(ENTRY_PRICE)
        .timestamp(new Date())
        .build();

    processor.tick(baseJob().build(), ticker);

    verifySentMessage();
    verifyWillRepeatWithoutChange(baseJob().build());
    verifyDidNothingElse();
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testActiveStopFalseDefault1() throws Exception {
    final Ticker ticker = everythingAt(STOP_PRICE.add(PENNY));

    processor.tick(baseJob().build(), ticker);

    verifyWillRepeatWithoutChange(baseJob().build());
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueDefault1() throws Exception {
    final Ticker ticker = everythingAt(STOP_PRICE);

    processor.tick(baseJob().build(), ticker);

    verifyLimitSellAtLimitPrice(ticker);
    verifySentMessage();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseDefault2() throws Exception {
    final Ticker ticker = new Ticker.Builder()
        .bid(STOP_PRICE.add(PENNY))
        .last(STOP_PRICE)
        .ask(STOP_PRICE.add(PENNY).add(PENNY))
        .timestamp(new Date()).build();

    processor.tick(baseJob().build(), ticker);

    verifyWillRepeatWithoutChange(baseJob().build());
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueDefault2() throws Exception {
    final Ticker ticker = new Ticker.Builder()
        .bid(STOP_PRICE)
        .last(STOP_PRICE.add(PENNY))
        .ask(STOP_PRICE.add(PENNY))
        .timestamp(new Date()).build();

    processor.tick(baseJob().build(), ticker);

    verifyLimitSellAtLimitPrice(ticker);
    verifySentMessage();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseAdjusted1() throws Exception {
    final Ticker ticker = everythingAt(HIGHER_STOP_PRICE.add(PENNY));
    SoftTrailingStop stop = baseJob().lastSyncPrice(HIGHER_ENTRY_PRICE).build();
    processor.tick(stop, ticker);

    verifyWillRepeatWithoutChange(stop);
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueAdjusted1() throws Exception {
    final Ticker ticker = everythingAt(HIGHER_STOP_PRICE);
    SoftTrailingStop stop = baseJob().lastSyncPrice(HIGHER_ENTRY_PRICE).build();
    processor.tick(stop, ticker);

    verifyLimitSellAtLimitPrice(ticker);
    verifySentMessage();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseAdjusted2() throws Exception {
    final Ticker ticker = new Ticker.Builder()
        .bid(HIGHER_STOP_PRICE.add(PENNY))
        .last(HIGHER_STOP_PRICE)
        .ask(HIGHER_STOP_PRICE.add(PENNY).add(PENNY))
        .timestamp(new Date()).build();
    SoftTrailingStop stop = baseJob().lastSyncPrice(HIGHER_ENTRY_PRICE).build();

    processor.tick(stop, ticker);

    verifyWillRepeatWithoutChange(stop);
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueAdjusted2() throws Exception {
    final Ticker ticker = new Ticker.Builder()
        .bid(HIGHER_STOP_PRICE)
        .last(HIGHER_STOP_PRICE.add(PENNY))
        .ask(HIGHER_STOP_PRICE.add(PENNY))
        .timestamp(new Date()).build();
    SoftTrailingStop stop = baseJob().lastSyncPrice(HIGHER_ENTRY_PRICE).build();

    processor.tick(stop, ticker);

    verifyLimitSellAtLimitPrice(ticker);
    verifySentMessage();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseRounding() throws Exception {
    final SoftTrailingStop job = baseJob()
        .stopPercentage(new BigDecimal("33.333333"))
        .limitPercentage(new BigDecimal("40.111111"))
        .build();

    final Ticker ticker = everythingAt(new BigDecimal("66.68"));

    processor.tick(job, ticker);

    verifyWillRepeatWithoutChange(job);
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueRounding() throws Exception {
    final SoftTrailingStop job = baseJob()
        .stopPercentage(new BigDecimal("33.333333"))
        .limitPercentage(new BigDecimal("40.111111"))
        .build();

    final Ticker ticker = everythingAt(new BigDecimal("66.67"));

    processor.tick(job, ticker);

    verifyLimitSellAt(ticker, new BigDecimal("59.89"));
    verifySentMessage();
    verifyDidNothingElse();
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testActivePriceChangeFalseDefault() throws Exception {
    final Ticker ticker = everythingAt(ENTRY_PRICE);

    processor.tick(baseJob().build(), ticker);

    verifyWillRepeatWithoutChange(baseJob().build());
    verifyDidNothingElse();
  }

  @Test
  public void testActivePriceChangeDownDefault() throws Exception {
    final Ticker ticker = everythingAt(ENTRY_PRICE.subtract(PENNY));

    processor.tick(baseJob().build(), ticker);

    verifyWillRepeatWithoutChange(baseJob().build());
    verifyDidNothingElse();
  }

  @Test
  public void testActivePriceChangeUpDefault() throws Exception {
    final Ticker ticker = new Ticker.Builder()
        .bid(ENTRY_PRICE.add(PENNY))
        .last(ENTRY_PRICE)
        .ask(ENTRY_PRICE.add(PENNY).add(PENNY))
        .timestamp(new Date()).build();
    final SoftTrailingStop job = baseJob().build();

    processor.tick(job, ticker);

    verify(exchangeService).get(EXCHANGE);
    verifyResyncedPriceTo(job, ENTRY_PRICE.add(PENNY));
    verifyDidNothingElse();
  }

  @Test
  public void testActivePriceChangeFalseAdjusted() throws Exception {
    final Ticker ticker = everythingAt(HIGHER_ENTRY_PRICE);

    SoftTrailingStop stop = baseJob().lastSyncPrice(HIGHER_ENTRY_PRICE).build();
    processor.tick(stop, ticker);

    verifyWillRepeatWithoutChange(stop);
    verifyDidNothingElse();
  }

  @Test
  public void testActivePriceChangeDownAdjusted() throws Exception {
    final Ticker ticker = everythingAt(HIGHER_ENTRY_PRICE.subtract(PENNY));

    SoftTrailingStop stop = baseJob().lastSyncPrice(HIGHER_ENTRY_PRICE).build();
    processor.tick(stop, ticker);

    verifyWillRepeatWithoutChange(stop);
    verifyDidNothingElse();
  }

  @Test
  public void testActivePriceChangeUpAdjusted() throws Exception {
    final Ticker ticker = new Ticker.Builder()
        .bid(HIGHER_ENTRY_PRICE.add(PENNY))
        .last(HIGHER_ENTRY_PRICE)
        .ask(HIGHER_ENTRY_PRICE.add(PENNY).add(PENNY))
        .timestamp(new Date()).build();
    final SoftTrailingStop job = baseJob().lastSyncPrice(HIGHER_ENTRY_PRICE).build();

    processor.tick(job, ticker);

    verify(exchangeService).get(EXCHANGE);
    verifyResyncedPriceTo(job, HIGHER_ENTRY_PRICE.add(PENNY));
    verifyDidNothingElse();
  }

  /* ---------------------------------- Utility methods  ---------------------------------------------------- */

  private void verifyResyncedPriceTo(SoftTrailingStop job, BigDecimal syncPrice) throws IOException {
    verify(enqueuer).enqueueAfterConfiguredDelay(job.toBuilder().lastSyncPrice(syncPrice).build());
  }

  private void verifyWillRepeatWithoutChange(SoftTrailingStop job) {
    verify(enqueuer).enqueueAfterConfiguredDelay(job);
  }

  private void verifyDidNothingElse() {
    verifyNoMoreInteractions(telegramService, tradeService, enqueuer);
  }

  private void verifySentMessage() {
    verify(telegramService).sendMessage(Mockito.anyString());
  }

  private void verifyLimitSellAtLimitPrice(final Ticker ticker) throws IOException {
    verifyLimitSellAt(ticker, LIMIT_PRICE);
  }

  private void verifyLimitSellAt(final Ticker ticker, BigDecimal price) throws IOException {
    verify(tradeService).placeLimitOrder(limitSell(AMOUNT, null, ticker.getTimestamp(), price));
    verify(enqueuer).enqueue(OrderStateNotifier.builder()
        .id(advancedOrderId.get())
        .description("Stop")
        .orderId(lastTradeId())
        .basic(AdvancedOrderInfo.builder().exchange(EXCHANGE).base(BASE).counter(COUNTER).build())
        .build());
  }

  private LimitOrder limitSell(BigDecimal amount, String id, Date timestamp, BigDecimal price) {
    return new LimitOrder(Order.OrderType.ASK, amount, CURRENCY_PAIR, id, timestamp, price);
  }

  private String newTradeId() {
    return Integer.toString(xChangeOrderId.incrementAndGet());
  }

  private String lastTradeId() {
    return Integer.toString(xChangeOrderId.get());
  }

  private Ticker everythingAt(BigDecimal price) {
    return new Ticker.Builder().bid(price).last(price).ask(price).timestamp(new Date()).build();
  }

  private SoftTrailingStop.Builder baseJob() {
    return SoftTrailingStop.builder()
      .id(JOB_ID)
      .amount(AMOUNT)
      .startPrice(ENTRY_PRICE)
      .stopPercentage(STOP_PC)
      .limitPercentage(LIMIT_PC)
      .basic(AdvancedOrderInfo.builder()
        .base(BASE)
        .counter(COUNTER)
        .exchange(EXCHANGE)
        .build()
       );
  }
}