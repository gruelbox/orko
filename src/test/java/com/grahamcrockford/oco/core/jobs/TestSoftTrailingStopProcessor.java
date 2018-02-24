package com.grahamcrockford.oco.core.jobs;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.grahamcrockford.oco.api.TickerSpec;
import com.grahamcrockford.oco.core.ExchangeService;
import com.grahamcrockford.oco.core.JobSubmitter;
import com.grahamcrockford.oco.core.TelegramService;
import com.grahamcrockford.oco.core.jobs.SoftTrailingStop;
import com.grahamcrockford.oco.core.jobs.SoftTrailingStopProcessor;
import com.grahamcrockford.oco.util.Sleep;

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
  @Mock private Sleep sleep;
  @Mock private CurrencyPairMetaData currencyPairMetaData;

  private SoftTrailingStopProcessor processor;

  @Before
  public void before() throws IOException {

    MockitoAnnotations.initMocks(this);

    when(currencyPairMetaData.getPriceScale()).thenReturn(PRICE_SCALE);

    processor = new SoftTrailingStopProcessor(telegramService, exchangeService, enqueuer, sleep);
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testErrNoBuyers() throws Exception {
    SoftTrailingStop job = baseJob().build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(new Ticker.Builder()
        .last(ENTRY_PRICE)
        .ask(ENTRY_PRICE.add(PENNY))
        .timestamp(new Date())
        .build());

    Optional<SoftTrailingStop> result = processor.process(job);

    verifySentMessage();
    verifyWillRepeatWithoutChange(job, result);
    verifyDidNothingElse();
  }

  @Test
  public void testErrNoSellers() throws Exception {
    SoftTrailingStop job = baseJob().build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(new Ticker.Builder()
        .last(ENTRY_PRICE)
        .ask(ENTRY_PRICE.add(PENNY))
        .timestamp(new Date())
        .build());

    Optional<SoftTrailingStop> result = processor.process(job);

    verifySentMessage();
    verifyWillRepeatWithoutChange(job, result);
    verifyDidNothingElse();
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testActiveStopFalseDefault1() throws Exception {
    SoftTrailingStop job = baseJob().build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(everythingAt(STOP_PRICE.add(PENNY)));

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyWillRepeatWithoutChange(job, result);
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueDefault1() throws Exception {
    SoftTrailingStop job = baseJob().build();
    Ticker ticker = everythingAt(STOP_PRICE);
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(ticker);

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyLimitSellAtLimitPrice(ticker);
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseDefault2() throws Exception {
    SoftTrailingStop job = baseJob().build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(new Ticker.Builder()
        .bid(STOP_PRICE.add(PENNY))
        .last(STOP_PRICE)
        .ask(STOP_PRICE.add(PENNY).add(PENNY))
        .timestamp(new Date()).build());

    Optional<SoftTrailingStop> result = processor.process(baseJob().build());

    verifyWillRepeatWithoutChange(job, result);
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueDefault2() throws Exception {
    SoftTrailingStop job = baseJob().build();
    final Ticker ticker = new Ticker.Builder()
        .bid(STOP_PRICE)
        .last(STOP_PRICE.add(PENNY))
        .ask(STOP_PRICE.add(PENNY))
        .timestamp(new Date()).build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(ticker);

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyLimitSellAtLimitPrice(ticker);
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseAdjusted1() throws Exception {
    final Ticker ticker = everythingAt(HIGHER_STOP_PRICE.add(PENNY));
    SoftTrailingStop job = baseJob().lastSyncPrice(HIGHER_ENTRY_PRICE).build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(ticker);

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyWillRepeatWithoutChange(job, result);
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueAdjusted1() throws Exception {
    final Ticker ticker = everythingAt(HIGHER_STOP_PRICE);
    SoftTrailingStop job = baseJob()
        .lastSyncPrice(HIGHER_ENTRY_PRICE)
        .stopPrice(STOP_PRICE.add(HIGHER_ENTRY_PRICE).subtract(ENTRY_PRICE))
        .build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(ticker);

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyLimitSellAtLimitPrice(ticker);
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseAdjusted2() throws Exception {
    final Ticker ticker = new Ticker.Builder()
        .bid(HIGHER_STOP_PRICE.add(PENNY))
        .last(HIGHER_STOP_PRICE)
        .ask(HIGHER_STOP_PRICE.add(PENNY).add(PENNY))
        .timestamp(new Date()).build();
    SoftTrailingStop job = baseJob()
        .lastSyncPrice(HIGHER_ENTRY_PRICE)
        .stopPrice(HIGHER_STOP_PRICE)
        .build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(ticker);

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyWillRepeatWithoutChange(job, result);
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueAdjusted2() throws Exception {
    final Ticker ticker = new Ticker.Builder()
        .bid(HIGHER_STOP_PRICE)
        .last(HIGHER_STOP_PRICE.add(PENNY))
        .ask(HIGHER_STOP_PRICE.add(PENNY))
        .timestamp(new Date()).build();
    SoftTrailingStop job = baseJob()
        .lastSyncPrice(HIGHER_ENTRY_PRICE)
        .stopPrice(HIGHER_STOP_PRICE)
        .build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(ticker);

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyLimitSellAtLimitPrice(ticker);
    verifyFinished(result);
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseRounding() throws Exception {
    final SoftTrailingStop job = baseJob()
        .stopPrice(new BigDecimal("66.674"))
        .limitPrice(new BigDecimal("50"))
        .build();

    final Ticker ticker = everythingAt(new BigDecimal("66.68"));
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(ticker);

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyWillRepeatWithoutChange(job, result);
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueRounding() throws Exception {
    final SoftTrailingStop job = baseJob()
        .stopPrice(new BigDecimal("66.674"))
        .limitPrice(new BigDecimal("50"))
        .build();

    final Ticker ticker = everythingAt(new BigDecimal("66.67"));
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(ticker);

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyLimitSellAt(ticker, new BigDecimal("50"));
    verifyFinished(result);
    verifyDidNothingElse();
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testActivePriceChangeFalseDefault() throws Exception {
    SoftTrailingStop job = baseJob().build();
    final Ticker ticker = everythingAt(ENTRY_PRICE);
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(ticker);

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyWillRepeatWithoutChange(job, result);
    verifyDidNothingElse();
  }

  @Test
  public void testActivePriceChangeDownDefault() throws Exception {
    SoftTrailingStop job = baseJob().build();
    final Ticker ticker = everythingAt(ENTRY_PRICE.subtract(PENNY));
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(ticker);

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyWillRepeatWithoutChange(job, result);
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
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(ticker);

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyResyncedPriceTo(job, result, ENTRY_PRICE.add(PENNY));
    verifyDidNothingElse();
  }

  @Test
  public void testActivePriceChangeFalseAdjusted() throws Exception {
    final Ticker ticker = everythingAt(HIGHER_ENTRY_PRICE);
    SoftTrailingStop job = baseJob().lastSyncPrice(HIGHER_ENTRY_PRICE).build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(ticker);

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyWillRepeatWithoutChange(job, result);
    verifyDidNothingElse();
  }

  @Test
  public void testActivePriceChangeDownAdjusted() throws Exception {
    final Ticker ticker = everythingAt(HIGHER_ENTRY_PRICE.subtract(PENNY));
    SoftTrailingStop job = baseJob().lastSyncPrice(HIGHER_ENTRY_PRICE).build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(ticker);

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyWillRepeatWithoutChange(job, result);
    verifyDidNothingElse();
  }

  @Test
  public void testActivePriceChangeUpAdjusted() throws Exception {
    final Ticker ticker = new Ticker.Builder()
        .bid(HIGHER_ENTRY_PRICE.add(PENNY))
        .last(HIGHER_ENTRY_PRICE)
        .ask(HIGHER_ENTRY_PRICE.add(PENNY).add(PENNY))
        .timestamp(new Date()).build();
    final SoftTrailingStop job = baseJob()
        .lastSyncPrice(HIGHER_ENTRY_PRICE)
        .stopPrice(STOP_PRICE.add(HIGHER_ENTRY_PRICE).subtract(ENTRY_PRICE))
        .build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(ticker);

    Optional<SoftTrailingStop> result = processor.process(job);

    verifyResyncedPriceTo(job, result, HIGHER_ENTRY_PRICE.add(PENNY));
    verifyDidNothingElse();
  }

  /* ---------------------------------- Utility methods  ---------------------------------------------------- */

  private void verifyResyncedPriceTo(SoftTrailingStop job, Optional<SoftTrailingStop> result, BigDecimal syncPrice) throws IOException {
    Assert.assertEquals(
      job.toBuilder()
        .lastSyncPrice(syncPrice)
        .stopPrice(job.stopPrice().add(syncPrice).subtract(job.lastSyncPrice()))
        .build(),
      result.get()
    );
  }

  private void verifyWillRepeatWithoutChange(SoftTrailingStop job, Optional<SoftTrailingStop> result) {
    Assert.assertTrue(result.isPresent());
    Assert.assertEquals(job, result.get());
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
      LimitSell.builder()
        .tickTrigger(TickerSpec.builder()
            .exchange(EXCHANGE)
            .counter(COUNTER)
            .base(BASE)
            .build()
        )
        .amount(AMOUNT)
        .limitPrice(price)
        .build()
    );
  }

  private void verifyFinished(Optional<SoftTrailingStop> result) {
    Assert.assertFalse(result.isPresent());
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