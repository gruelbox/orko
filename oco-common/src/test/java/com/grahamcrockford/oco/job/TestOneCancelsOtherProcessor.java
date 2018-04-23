package com.grahamcrockford.oco.job;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.function.BiConsumer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.grahamcrockford.oco.job.OneCancelsOther.ThresholdAndJob;
import com.grahamcrockford.oco.spi.Job;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.submit.JobSubmitter;
import com.grahamcrockford.oco.telegram.TelegramService;
import com.grahamcrockford.oco.ticker.ExchangeEventRegistry;

public class TestOneCancelsOtherProcessor {

  private static final BigDecimal LOW_PRICE = new BigDecimal(100);
  private static final BigDecimal HIGH_PRICE = new BigDecimal(200);

  private static final String JOB_ID = "XXX";
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

  @Mock private Exchange exchange;
  @Mock private MarketDataService marketDataService;
  @Mock private JobControl jobControl;
  @Mock private ExchangeEventRegistry exchangeEventRegistry;

  @Mock private Job job1;
  @Mock private Job job2;

  @Captor private ArgumentCaptor<BiConsumer<TickerSpec, Ticker>> tickerConsumerCaptor;

  @Before
  public void before() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testAtLowNoJob() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .id(JOB_ID)
        .tickTrigger(TICKER_SPEC)
        .high(ThresholdAndJob.create(HIGH_PRICE, job2))
        .build();

    OneCancelsOtherProcessor processor = new OneCancelsOtherProcessor(job, jobControl, enqueuer, telegramService, exchangeEventRegistry);
    Assert.assertTrue(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(job.tickTrigger(), new Ticker.Builder()
        .bid(LOW_PRICE)
        .build());

    verifyDidNothingElse();
  }


  @Test
  public void testAtLow() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .id(JOB_ID)
        .tickTrigger(TICKER_SPEC)
        .low(ThresholdAndJob.create(LOW_PRICE, job1))
        .high(ThresholdAndJob.create(HIGH_PRICE, job2))
        .build();

    OneCancelsOtherProcessor processor = new OneCancelsOtherProcessor(job, jobControl, enqueuer, telegramService, exchangeEventRegistry);
    Assert.assertTrue(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(job.tickTrigger(), new Ticker.Builder()
        .bid(LOW_PRICE)
        .build());

    verify(telegramService).sendMessage(Mockito.anyString());
    verify(enqueuer).submitNewUnchecked(job1);
    verify(jobControl).finish();
    verifyDidNothingElse();
  }

  @Test
  public void testAtLowQuiet() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .id(JOB_ID)
        .tickTrigger(TICKER_SPEC)
        .low(ThresholdAndJob.create(LOW_PRICE, job1))
        .high(ThresholdAndJob.create(HIGH_PRICE, job2))
        .verbose(false)
        .build();

    OneCancelsOtherProcessor processor = new OneCancelsOtherProcessor(job, jobControl, enqueuer, telegramService, exchangeEventRegistry);
    Assert.assertTrue(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(job.tickTrigger(), new Ticker.Builder()
        .bid(LOW_PRICE)
        .build());

    verify(enqueuer).submitNewUnchecked(job1);
    verify(jobControl).finish();
    verifyDidNothingElse();
  }

  @Test
  public void testAboveLow() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .id(JOB_ID)
        .tickTrigger(TICKER_SPEC)
        .low(ThresholdAndJob.create(LOW_PRICE, job1))
        .high(ThresholdAndJob.create(HIGH_PRICE, job2))
        .build();

    OneCancelsOtherProcessor processor = new OneCancelsOtherProcessor(job, jobControl, enqueuer, telegramService, exchangeEventRegistry);
    Assert.assertTrue(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(job.tickTrigger(), new Ticker.Builder()
        .bid(LOW_PRICE.add(BigDecimal.ONE))
        .build());

    verifyDidNothingElse();
  }

  @Test
  public void testBelowHigh() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .id(JOB_ID)
        .tickTrigger(TICKER_SPEC)
        .low(ThresholdAndJob.create(LOW_PRICE, job1))
        .high(ThresholdAndJob.create(HIGH_PRICE, job2))
        .build();

    OneCancelsOtherProcessor processor = new OneCancelsOtherProcessor(job, jobControl, enqueuer, telegramService, exchangeEventRegistry);
    Assert.assertTrue(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(job.tickTrigger(), new Ticker.Builder()
        .bid(HIGH_PRICE.subtract(BigDecimal.ONE))
        .build());

    verifyDidNothingElse();
  }

  @Test
  public void testAtHigh() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .id(JOB_ID)
        .tickTrigger(TICKER_SPEC)
        .low(ThresholdAndJob.create(LOW_PRICE, job1))
        .high(ThresholdAndJob.create(HIGH_PRICE, job2))
        .build();

    OneCancelsOtherProcessor processor = new OneCancelsOtherProcessor(job, jobControl, enqueuer, telegramService, exchangeEventRegistry);
    Assert.assertTrue(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(job.tickTrigger(), new Ticker.Builder()
        .bid(HIGH_PRICE)
        .build());

    verify(telegramService).sendMessage(Mockito.anyString());
    verify(enqueuer).submitNewUnchecked(job2);
    verify(jobControl).finish();
    verifyDidNothingElse();
  }

  @Test
  public void testAtHighQuiet() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .id(JOB_ID)
        .tickTrigger(TICKER_SPEC)
        .low(ThresholdAndJob.create(LOW_PRICE, job1))
        .high(ThresholdAndJob.create(HIGH_PRICE, job2))
        .verbose(false)
        .build();

    OneCancelsOtherProcessor processor = new OneCancelsOtherProcessor(job, jobControl, enqueuer, telegramService, exchangeEventRegistry);
    Assert.assertTrue(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(job.tickTrigger(), new Ticker.Builder()
        .bid(HIGH_PRICE)
        .build());

    verify(enqueuer).submitNewUnchecked(job2);
    verify(jobControl).finish();
    verifyDidNothingElse();
  }

  @Test
  public void testAtHighNoHighJob() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .id(JOB_ID)
        .tickTrigger(TICKER_SPEC)
        .low(ThresholdAndJob.create(LOW_PRICE, job1))
        .build();

    OneCancelsOtherProcessor processor = new OneCancelsOtherProcessor(job, jobControl, enqueuer, telegramService, exchangeEventRegistry);
    Assert.assertTrue(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(job.tickTrigger(), new Ticker.Builder()
        .bid(HIGH_PRICE)
        .build());

    verifyDidNothingElse();
  }

  private void verifyDidNothingElse() {
    verifyNoMoreInteractions(exchangeEventRegistry, telegramService, enqueuer, jobControl);
  }
}