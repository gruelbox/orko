package com.grahamcrockford.oco.core.jobs;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.grahamcrockford.oco.core.api.ExchangeService;
import com.grahamcrockford.oco.core.impl.JobSubmitter;
import com.grahamcrockford.oco.core.jobs.OneCancelsOther.ThresholdAndJob;
import com.grahamcrockford.oco.core.spi.Job;
import com.grahamcrockford.oco.core.spi.TickerSpec;
import com.grahamcrockford.oco.telegram.TelegramService;
import com.grahamcrockford.oco.util.Sleep;

public class TestOneCancelsOtherProcessor {

  private static final BigDecimal LOW_PRICE = new BigDecimal(100);
  private static final BigDecimal HIGH_PRICE = new BigDecimal(200);

  private static final String BASE = "FOO";
  private static final String COUNTER = "USDT";
  private static final String EXCHANGE = "fooex";
  private static final TickerSpec TICKER_SPEC = TickerSpec.builder()
      .exchange(EXCHANGE)
      .counter(COUNTER)
      .base(BASE)
      .build();

  @Mock private JobSubmitter enqueuer;
  @Mock private TelegramService telegramService;
  @Mock private ExchangeService exchangeService;

  @Mock private Exchange exchange;
  @Mock private MarketDataService marketDataService;
  @Mock private Sleep sleep;

  @Mock private Job job1;
  @Mock private Job job2;

  private OneCancelsOtherProcessor processor;

  @Before
  public void before() throws IOException {
    MockitoAnnotations.initMocks(this);
    processor = new OneCancelsOtherProcessor(exchangeService, enqueuer, telegramService, sleep);
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testAtLowNoJob() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .tickTrigger(TICKER_SPEC)
        .high(ThresholdAndJob.create(HIGH_PRICE, job2))
        .build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(new Ticker.Builder()
        .bid(LOW_PRICE)
        .build());

    Optional<OneCancelsOther> result = processor.process(job);

    Assert.assertTrue(result.isPresent());

    verify(exchangeService).fetchTicker(TICKER_SPEC);
    verifyNoMoreInteractions(telegramService, enqueuer);
  }

  @Test
  public void testAtLow() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .tickTrigger(TICKER_SPEC)
        .low(ThresholdAndJob.create(LOW_PRICE, job1))
        .high(ThresholdAndJob.create(HIGH_PRICE, job2))
        .build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(new Ticker.Builder()
        .bid(LOW_PRICE)
        .build());

    Optional<OneCancelsOther> result = processor.process(job);

    Assert.assertFalse(result.isPresent());

    verify(exchangeService).fetchTicker(TICKER_SPEC);
    verify(telegramService).sendMessage(Mockito.anyString());
    verify(enqueuer).submitNew(job1);
    verifyNoMoreInteractions(telegramService, enqueuer);
  }

  @Test
  public void testAboveLow() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .tickTrigger(TICKER_SPEC)
        .low(ThresholdAndJob.create(LOW_PRICE, job1))
        .high(ThresholdAndJob.create(HIGH_PRICE, job2))
        .build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(new Ticker.Builder()
        .bid(LOW_PRICE.add(BigDecimal.ONE))
        .build());

    Optional<OneCancelsOther> result = processor.process(job);

    Assert.assertTrue(result.isPresent());
    verify(exchangeService).fetchTicker(TICKER_SPEC);
    verifyNoMoreInteractions(telegramService, enqueuer);
  }

  @Test
  public void testBelowHigh() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .tickTrigger(TICKER_SPEC)
        .low(ThresholdAndJob.create(LOW_PRICE, job1))
        .high(ThresholdAndJob.create(HIGH_PRICE, job2))
        .build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(new Ticker.Builder()
        .bid(HIGH_PRICE.subtract(BigDecimal.ONE))
        .build());

    Optional<OneCancelsOther> result = processor.process(job);

    Assert.assertTrue(result.isPresent());
    verify(exchangeService).fetchTicker(TICKER_SPEC);
    verifyNoMoreInteractions(telegramService, enqueuer);
  }

  @Test
  public void testAtHigh() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .tickTrigger(TICKER_SPEC)
        .low(ThresholdAndJob.create(LOW_PRICE, job1))
        .high(ThresholdAndJob.create(HIGH_PRICE, job2))
        .build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(new Ticker.Builder()
        .bid(HIGH_PRICE)
        .build());

    Optional<OneCancelsOther> result = processor.process(job);

    Assert.assertFalse(result.isPresent());

    verify(exchangeService).fetchTicker(TICKER_SPEC);
    verify(telegramService).sendMessage(Mockito.anyString());
    verify(enqueuer).submitNew(job2);
    verifyNoMoreInteractions(telegramService, enqueuer);
  }

  @Test
  public void testAtHighNoHighJob() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .tickTrigger(TICKER_SPEC)
        .low(ThresholdAndJob.create(LOW_PRICE, job1))
        .build();
    when(exchangeService.fetchTicker(job.tickTrigger())).thenReturn(new Ticker.Builder()
        .bid(HIGH_PRICE)
        .build());

    Optional<OneCancelsOther> result = processor.process(job);

    Assert.assertTrue(result.isPresent());

    verify(exchangeService).fetchTicker(TICKER_SPEC);
    verifyNoMoreInteractions(telegramService, enqueuer);
  }
}