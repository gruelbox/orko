package com.grahamcrockford.oco.job;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.RandomUtils;
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

import com.grahamcrockford.oco.exchange.ExchangeService;
import com.grahamcrockford.oco.job.LimitOrderJob.Direction;
import com.grahamcrockford.oco.job.SoftTrailingStop.Builder;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.submit.JobSubmitter;
import com.grahamcrockford.oco.telegram.TelegramService;
import com.grahamcrockford.oco.ticker.ExchangeEventRegistry;

public class TestSoftTrailingStopProcessorBuy {

  private static final int PRICE_SCALE = 2;
  private static final BigDecimal HUNDRED = new BigDecimal(100);
  private static final BigDecimal PENNY = new BigDecimal("0.01");

  private static final BigDecimal AMOUNT = new BigDecimal(1000);

  private static final BigDecimal ENTRY_PRICE = HUNDRED;
  private static final BigDecimal STOP_PRICE = ENTRY_PRICE.add(new BigDecimal(RandomUtils.nextInt(2, 10)));
  private static final BigDecimal LIMIT_PRICE = STOP_PRICE.add(new BigDecimal(RandomUtils.nextInt(2, 10)));

  private static final BigDecimal LOWER_ENTRY_PRICE = ENTRY_PRICE.subtract(new BigDecimal(RandomUtils.nextInt(2, 50)));
  private static final BigDecimal ADJUSTED_STOP_PRICE = STOP_PRICE.subtract(ENTRY_PRICE).add(LOWER_ENTRY_PRICE);

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

    processor.tick(job.tickTrigger(), new Ticker.Builder()
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

    processor.tick(job.tickTrigger(), new Ticker.Builder()
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
    processor.tick(job.tickTrigger(), everythingAt(STOP_PRICE.subtract(PENNY)));

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueDefault1() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    Ticker ticker = everythingAt(STOP_PRICE);
    processor.tick(job.tickTrigger(), ticker);

    verifyLimitBuyAtLimitPrice(ticker);
    verifyFinished();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseDefault2() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    processor.tick(job.tickTrigger(), new Ticker.Builder()
        .bid(STOP_PRICE.subtract(PENNY).subtract(PENNY))
        .last(STOP_PRICE)
        .ask(STOP_PRICE.subtract(PENNY))
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
        .bid(STOP_PRICE.subtract(PENNY).subtract(PENNY))
        .last(STOP_PRICE)
        .ask(STOP_PRICE)
        .timestamp(new Date()).build();
    processor.tick(job.tickTrigger(), ticker);

    verifyLimitBuyAtLimitPrice(ticker);
    verifyFinished();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseAdjusted1() throws Exception {
    SoftTrailingStop job = jobAlreadyAdjusted().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = everythingAt(ADJUSTED_STOP_PRICE.subtract(PENNY));
    processor.tick(job.tickTrigger(), ticker);

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }


  @Test
  public void testActiveStopTrueAdjusted1() throws Exception {
    SoftTrailingStop job = jobAlreadyAdjusted().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = everythingAt(ADJUSTED_STOP_PRICE);
    processor.tick(job.tickTrigger(), ticker);

    verifyLimitBuyAtLimitPrice(ticker);
    verifyFinished();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseAdjusted2() throws Exception {
    SoftTrailingStop job = jobAlreadyAdjusted().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = new Ticker.Builder()
        .bid(ADJUSTED_STOP_PRICE.subtract(PENNY).subtract(PENNY))
        .last(ADJUSTED_STOP_PRICE)
        .ask(ADJUSTED_STOP_PRICE.subtract(PENNY))
        .timestamp(new Date()).build();
    processor.tick(job.tickTrigger(), ticker);

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueAdjusted2() throws Exception {
    SoftTrailingStop job = jobAlreadyAdjusted().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = new Ticker.Builder()
        .bid(ADJUSTED_STOP_PRICE.subtract(PENNY).subtract(PENNY))
        .last(ADJUSTED_STOP_PRICE)
        .ask(ADJUSTED_STOP_PRICE)
        .timestamp(new Date()).build();
    processor.tick(job.tickTrigger(), ticker);

    verifyLimitBuyAtLimitPrice(ticker);
    verifyFinished();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseRounding() throws Exception {
    final SoftTrailingStop job = baseJob()
        .stopPrice(new BigDecimal("156.675"))
        .limitPrice(new BigDecimal("160"))
        .build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = everythingAt(new BigDecimal("156.67"));
    processor.tick(job.tickTrigger(), ticker);

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueRounding() throws Exception {
    final SoftTrailingStop job = baseJob()
        .stopPrice(new BigDecimal("156.675"))
        .limitPrice(new BigDecimal("160"))
        .build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = everythingAt(new BigDecimal("156.68"));
    processor.tick(job.tickTrigger(), ticker);

    verifyLimitBuyAt(ticker, new BigDecimal("160"));
    verifyFinished();
    verifyDidNothingElse();
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testActiveAdjustNoChange() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = everythingAt(ENTRY_PRICE);
    processor.tick(job.tickTrigger(), ticker);

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveAdjustPriceRise() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);
    start(job, processor);

    final Ticker ticker = everythingAt(ENTRY_PRICE.add(PENNY));
    processor.tick(job.tickTrigger(), ticker);

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveAdjustPriceDrop() throws Exception {
    final SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = new Ticker.Builder()
        .bid(ENTRY_PRICE.subtract(PENNY).subtract(PENNY))
        .last(ENTRY_PRICE)
        .ask(ENTRY_PRICE.subtract(PENNY))
        .timestamp(new Date()).build();
    processor.tick(job.tickTrigger(), ticker);

    verifyResyncedPriceTo(job, ENTRY_PRICE.subtract(PENNY));
    verifyDidNothingElse();
  }

  @Test
  public void testActiveAdjustSecondTimeNoChange() throws Exception {
    SoftTrailingStop job = jobAlreadyAdjusted().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = everythingAt(LOWER_ENTRY_PRICE);
    processor.tick(job.tickTrigger(), ticker);

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveAdjustSecondTimePriceRise() throws Exception {
    SoftTrailingStop job = jobAlreadyAdjusted().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = everythingAt(LOWER_ENTRY_PRICE.add(PENNY));
    processor.tick(job.tickTrigger(), ticker);

    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveAdjustSecondTimePriceDrop() throws Exception {
    final SoftTrailingStop job = jobAlreadyAdjusted().build();
    SoftTrailingStopProcessor processor = processor(job);

    start(job, processor);

    final Ticker ticker = new Ticker.Builder()
        .bid(LOWER_ENTRY_PRICE.subtract(PENNY).subtract(PENNY))
        .last(LOWER_ENTRY_PRICE)
        .ask(LOWER_ENTRY_PRICE.subtract(PENNY))
        .timestamp(new Date()).build();
    processor.tick(job.tickTrigger(), ticker);

    verifyResyncedPriceTo(job, LOWER_ENTRY_PRICE.subtract(PENNY));
    verifyDidNothingElse();
  }

  /* ---------------------------------- Utility methods  ---------------------------------------------------- */

  @SuppressWarnings("unchecked")
  private void start(SoftTrailingStop job, SoftTrailingStopProcessor processor) {
    Assert.assertTrue(processor.start());
    verify(exchangeEventRegistry).registerTicker(Mockito.eq(job.tickTrigger()), Mockito.eq(job.id()), Mockito.any(BiConsumer.class));
  }

  private SoftTrailingStopProcessor processor(SoftTrailingStop job) {
    return new SoftTrailingStopProcessor(job, jobControl, telegramService, exchangeService, enqueuer, exchangeEventRegistry);
  }

  private void verifyResyncedPriceTo(SoftTrailingStop job, BigDecimal syncPrice) throws IOException {
    verify(jobControl).replace(
      job.toBuilder()
        .lastSyncPrice(syncPrice)
        .stopPrice(job.stopPrice().subtract(syncPrice).add(job.lastSyncPrice()))
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

  private void verifyLimitBuyAtLimitPrice(final Ticker ticker) throws Exception {
    verifyLimitBuyAt(ticker, LIMIT_PRICE);
  }

  private void verifyLimitBuyAt(final Ticker ticker, BigDecimal price) throws Exception {
    verify(enqueuer).submitNewUnchecked(
      LimitOrderJob.builder()
        .tickTrigger(TickerSpec.builder()
            .exchange(EXCHANGE)
            .counter(COUNTER)
            .base(BASE)
            .build()
        )
        .direction(Direction.BUY)
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
      .direction(Direction.BUY)
      .tickTrigger(ex);
  }

  private Builder jobAlreadyAdjusted() {
    return baseJob().lastSyncPrice(LOWER_ENTRY_PRICE).stopPrice(ADJUSTED_STOP_PRICE);
  }
}