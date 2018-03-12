package com.grahamcrockford.oco.core.jobs;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.grahamcrockford.oco.core.api.ExchangeEventRegistry;
import com.grahamcrockford.oco.core.api.ExchangeService;
import com.grahamcrockford.oco.core.api.JobSubmitter;
import com.grahamcrockford.oco.core.jobs.LimitOrderJob.Direction;
import com.grahamcrockford.oco.core.spi.JobControl;
import com.grahamcrockford.oco.core.spi.TickerSpec;
import com.grahamcrockford.oco.telegram.TelegramService;

public class TestSoftTrailingStopProcessor {

  private static final int PRICE_SCALE = 2;
  private static final BigDecimal HUNDRED = new BigDecimal(100);
  private static final BigDecimal PENNY = new BigDecimal("0.01");

  private static final BigDecimal AMOUNT = new BigDecimal(1000);

  private static final BigDecimal ENTRY_PRICE = HUNDRED;
  private static final BigDecimal STOP_PRICE = new BigDecimal(95);
  private static final BigDecimal LIMIT_PRICE = new BigDecimal(90);

  private static final BigDecimal HIGHER_ENTRY_PRICE = new BigDecimal(200);
  private static final BigDecimal HIGHER_STOP_PRICE = new BigDecimal(195);

  private static final String BASE = "FOO";
  private static final String COUNTER = "USDT";
  private static final String EXCHANGE = "fooex";

  @Mock private JobSubmitter enqueuer;
  @Mock private TelegramService telegramService;
  @Mock private ExchangeService exchangeService;

  @Mock private Exchange exchange;
  @Mock private MarketDataService marketDataService;
  @Mock private JobControl jobControl;
  @Mock private CurrencyPairMetaData currencyPairMetaData;
  @Mock private ExchangeEventRegistry exchangeEventRegistry;

  @Captor private ArgumentCaptor<Consumer<Ticker>> tickerConsumerCaptor;

  @Before
  public void before() throws IOException {
    MockitoAnnotations.initMocks(this);
    when(currencyPairMetaData.getPriceScale()).thenReturn(PRICE_SCALE);
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testErrNoBuyers() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    processor.tick(new Ticker.Builder()
        .last(ENTRY_PRICE)
        .ask(ENTRY_PRICE.add(PENNY))
        .timestamp(new Date())
        .build());

    verifySentMessage();
    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testErrNoSellers() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    processor.tick(new Ticker.Builder()
        .last(ENTRY_PRICE)
        .ask(ENTRY_PRICE.add(PENNY))
        .timestamp(new Date())
        .build());

    verifySentMessage();
    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testActiveStopFalseDefault1() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);
    processor.tick(everythingAt(STOP_PRICE.add(PENNY)));

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueDefault1() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    Ticker ticker = everythingAt(STOP_PRICE);
    processor.tick(ticker);

    verifyLimitSellAtLimitPrice(ticker);
    verifyFinished();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseDefault2() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    processor.tick(new Ticker.Builder()
        .bid(STOP_PRICE.add(PENNY))
        .last(STOP_PRICE)
        .ask(STOP_PRICE.add(PENNY).add(PENNY))
        .timestamp(new Date()).build());

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueDefault2() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = new Ticker.Builder()
        .bid(STOP_PRICE)
        .last(STOP_PRICE.add(PENNY))
        .ask(STOP_PRICE.add(PENNY))
        .timestamp(new Date()).build();
    processor.tick(ticker);

    verifyLimitSellAtLimitPrice(ticker);
    verifyFinished();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseAdjusted1() throws Exception {
    SoftTrailingStop job = baseJob().lastSyncPrice(HIGHER_ENTRY_PRICE).build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = everythingAt(HIGHER_STOP_PRICE.add(PENNY));
    processor.tick(ticker);

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueAdjusted1() throws Exception {
    SoftTrailingStop job = baseJob()
        .lastSyncPrice(HIGHER_ENTRY_PRICE)
        .stopPrice(STOP_PRICE.add(HIGHER_ENTRY_PRICE).subtract(ENTRY_PRICE))
        .build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = everythingAt(HIGHER_STOP_PRICE);
    processor.tick(ticker);

    verifyLimitSellAtLimitPrice(ticker);
    verifyFinished();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseAdjusted2() throws Exception {
    SoftTrailingStop job = baseJob()
        .lastSyncPrice(HIGHER_ENTRY_PRICE)
        .stopPrice(HIGHER_STOP_PRICE)
        .build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = new Ticker.Builder()
        .bid(HIGHER_STOP_PRICE.add(PENNY))
        .last(HIGHER_STOP_PRICE)
        .ask(HIGHER_STOP_PRICE.add(PENNY).add(PENNY))
        .timestamp(new Date()).build();
    processor.tick(ticker);

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueAdjusted2() throws Exception {
    SoftTrailingStop job = baseJob()
        .lastSyncPrice(HIGHER_ENTRY_PRICE)
        .stopPrice(HIGHER_STOP_PRICE)
        .build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = new Ticker.Builder()
        .bid(HIGHER_STOP_PRICE)
        .last(HIGHER_STOP_PRICE.add(PENNY))
        .ask(HIGHER_STOP_PRICE.add(PENNY))
        .timestamp(new Date()).build();
    processor.tick(ticker);

    verifyLimitSellAtLimitPrice(ticker);
    verifyFinished();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseRounding() throws Exception {
    final SoftTrailingStop job = baseJob()
        .stopPrice(new BigDecimal("66.674"))
        .limitPrice(new BigDecimal("50"))
        .build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = everythingAt(new BigDecimal("66.68"));
    processor.tick(ticker);

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueRounding() throws Exception {
    final SoftTrailingStop job = baseJob()
        .stopPrice(new BigDecimal("66.674"))
        .limitPrice(new BigDecimal("50"))
        .build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = everythingAt(new BigDecimal("66.67"));
    processor.tick(ticker);

    verifyLimitSellAt(ticker, new BigDecimal("50"));
    verifyFinished();
    verifyDidNothingElse();
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testActivePriceChangeFalseDefault() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = everythingAt(ENTRY_PRICE);
    processor.tick(ticker);

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActivePriceChangeDownDefault() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);
    start(job, processor);

    final Ticker ticker = everythingAt(ENTRY_PRICE.subtract(PENNY));
    processor.tick(ticker);

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActivePriceChangeUpDefault() throws Exception {
    final SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = new Ticker.Builder()
        .bid(ENTRY_PRICE.add(PENNY))
        .last(ENTRY_PRICE)
        .ask(ENTRY_PRICE.add(PENNY).add(PENNY))
        .timestamp(new Date()).build();
    processor.tick(ticker);

    verifyResyncedPriceTo(job, ENTRY_PRICE.add(PENNY));
    verifyDidNothingElse();
  }

  @Test
  public void testActivePriceChangeFalseAdjusted() throws Exception {
    SoftTrailingStop job = baseJob().lastSyncPrice(HIGHER_ENTRY_PRICE).build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = everythingAt(HIGHER_ENTRY_PRICE);
    processor.tick(ticker);

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActivePriceChangeDownAdjusted() throws Exception {
    SoftTrailingStop job = baseJob().lastSyncPrice(HIGHER_ENTRY_PRICE).build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = everythingAt(HIGHER_ENTRY_PRICE.subtract(PENNY));
    processor.tick(ticker);

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActivePriceChangeUpAdjusted() throws Exception {
    final SoftTrailingStop job = baseJob()
        .lastSyncPrice(HIGHER_ENTRY_PRICE)
        .stopPrice(STOP_PRICE.add(HIGHER_ENTRY_PRICE).subtract(ENTRY_PRICE))
        .build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = new Ticker.Builder()
        .bid(HIGHER_ENTRY_PRICE.add(PENNY))
        .last(HIGHER_ENTRY_PRICE)
        .ask(HIGHER_ENTRY_PRICE.add(PENNY).add(PENNY))
        .timestamp(new Date()).build();
    processor.tick(ticker);

    verifyResyncedPriceTo(job, HIGHER_ENTRY_PRICE.add(PENNY));
    verifyDidNothingElse();
  }

  /* ---------------------------------- Utility methods  ---------------------------------------------------- */

  @SuppressWarnings("unchecked")
  private void start(SoftTrailingStop job, SoftTrailingStopProcessor processor) {
    Assert.assertTrue(processor.start());
    verify(exchangeEventRegistry).registerTicker(Mockito.eq(job.tickTrigger()), Mockito.eq(job.id()), Mockito.any(Consumer.class));
  }

  private SoftTrailingStopProcessor processor(SoftTrailingStop job) {
    return new SoftTrailingStopProcessor(job, jobControl, telegramService, exchangeService, enqueuer, exchangeEventRegistry);
  }

  private void verifyResyncedPriceTo(SoftTrailingStop job, BigDecimal syncPrice) throws IOException {
    verify(jobControl).replace(
      job.toBuilder()
        .lastSyncPrice(syncPrice)
        .stopPrice(job.stopPrice().add(syncPrice).subtract(job.lastSyncPrice()))
        .build());
  }

  private void verifyWillRepeatWithoutChange() {
    verifyZeroInteractions(jobControl);
  }

  private void verifyDidNothingElse() {
    verifyNoMoreInteractions(telegramService, enqueuer);
  }

  private void verifySentMessage() {
    verify(telegramService).sendMessage(Mockito.anyString());
  }

  private void verifyLimitSellAtLimitPrice(final Ticker ticker) throws IOException {
    verifyLimitSellAt(ticker, LIMIT_PRICE);
  }

  private void verifyLimitSellAt(final Ticker ticker, BigDecimal price) throws IOException {
    verify(enqueuer).submitNew(
      LimitOrderJob.builder()
        .tickTrigger(TickerSpec.builder()
            .exchange(EXCHANGE)
            .counter(COUNTER)
            .base(BASE)
            .build()
        )
        .direction(Direction.SELL)
        .amount(AMOUNT)
        .limitPrice(price)
        .build()
    );
  }

  private void verifyFinished() {
    verify(jobControl).finish();
  }

  private Ticker everythingAt(BigDecimal price) {
    return new Ticker.Builder().bid(price).last(price).ask(price).timestamp(new Date()).build();
  }

  private SoftTrailingStop.Builder baseJob() {
    TickerSpec ex = TickerSpec.builder()
      .base(BASE)
      .counter(COUNTER)
      .exchange(EXCHANGE)
      .build();
    when(exchangeService.fetchCurrencyPairMetaData(ex)).thenReturn(currencyPairMetaData);
    return SoftTrailingStop.builder()
      .amount(AMOUNT)
      .startPrice(ENTRY_PRICE)
      .stopPrice(STOP_PRICE)
      .limitPrice(LIMIT_PRICE)
      .tickTrigger(ex);
  }
}