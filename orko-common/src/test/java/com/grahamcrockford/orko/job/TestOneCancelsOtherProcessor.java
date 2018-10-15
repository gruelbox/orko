package com.grahamcrockford.orko.job;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.grahamcrockford.orko.exchange.ExchangeService;
import com.grahamcrockford.orko.job.OneCancelsOther.ThresholdAndJob;
import com.grahamcrockford.orko.marketdata.ExchangeEventRegistry;
import com.grahamcrockford.orko.marketdata.TickerEvent;
import com.grahamcrockford.orko.notification.Notification;
import com.grahamcrockford.orko.notification.NotificationLevel;
import com.grahamcrockford.orko.notification.NotificationService;
import com.grahamcrockford.orko.notification.Status;
import com.grahamcrockford.orko.notification.StatusUpdateService;
import com.grahamcrockford.orko.spi.Job;
import com.grahamcrockford.orko.spi.JobControl;
import com.grahamcrockford.orko.spi.TickerSpec;
import com.grahamcrockford.orko.submit.JobSubmitter;

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
  @Mock private StatusUpdateService statusUpdateService;
  @Mock private NotificationService notificationService;

  @Mock private Exchange exchange;
  @Mock private MarketDataService marketDataService;
  @Mock private JobControl jobControl;
  @Mock private ExchangeEventRegistry exchangeEventRegistry;
  @Mock private ExchangeService exchangeService;

  @Mock private Job job1;
  @Mock private Job job2;

  @Captor private ArgumentCaptor<Consumer<TickerEvent>> tickerConsumerCaptor;

  @Before
  public void before() throws IOException {
    MockitoAnnotations.initMocks(this);
    when(exchangeService.exchangeSupportsPair(EXCHANGE, new CurrencyPair(BASE, COUNTER))).thenReturn(true);
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testAtLowNoJob() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .id(JOB_ID)
        .tickTrigger(TICKER_SPEC)
        .high(ThresholdAndJob.create(HIGH_PRICE, job2))
        .build();

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(
        TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
          .bid(LOW_PRICE)
          .build()));

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

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(
        TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
          .bid(LOW_PRICE)
          .build()));

    ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationService).send(notificationCaptor.capture());
    Assert.assertEquals(NotificationLevel.ALERT, notificationCaptor.getValue().level());
    verify(enqueuer).submitNewUnchecked(job1);
    verify(jobControl).finish(Status.SUCCESS);
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

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(
        TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
            .bid(LOW_PRICE)
            .build()));

    ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationService).send(notificationCaptor.capture());
    Assert.assertEquals(NotificationLevel.INFO, notificationCaptor.getValue().level());
    verify(enqueuer).submitNewUnchecked(job1);
    verify(jobControl).finish(Status.SUCCESS);
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

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(
        TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
          .bid(LOW_PRICE.add(BigDecimal.ONE))
          .build()));

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

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(
        TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
          .bid(HIGH_PRICE.subtract(BigDecimal.ONE))
          .build()));

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

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(
        TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
          .bid(HIGH_PRICE)
          .build()));

    ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationService).send(notificationCaptor.capture());
    Assert.assertEquals(NotificationLevel.ALERT, notificationCaptor.getValue().level());
    verify(enqueuer).submitNewUnchecked(job2);
    verify(jobControl).finish(Status.SUCCESS);
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

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(
        TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
          .bid(HIGH_PRICE)
          .build()));

    ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationService).send(notificationCaptor.capture());
    Assert.assertEquals(NotificationLevel.INFO, notificationCaptor.getValue().level());
    verify(enqueuer).submitNewUnchecked(job2);
    verify(jobControl).finish(Status.SUCCESS);
    verifyDidNothingElse();
  }

  @Test
  public void testAtHighNoHighJob() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .id(JOB_ID)
        .tickTrigger(TICKER_SPEC)
        .low(ThresholdAndJob.create(LOW_PRICE, job1))
        .build();

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    verify(exchangeEventRegistry).registerTicker(eq(job.tickTrigger()), eq(JOB_ID), tickerConsumerCaptor.capture());

    tickerConsumerCaptor.getValue().accept(
        TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
          .bid(HIGH_PRICE)
          .build()));

    verifyDidNothingElse();
  }

  @Test
  public void testCurrencyNotSupported() {
    when(exchangeService.exchangeSupportsPair(EXCHANGE, new CurrencyPair(BASE, COUNTER))).thenReturn(false);
    OneCancelsOther job = OneCancelsOther.builder()
        .id(JOB_ID)
        .tickTrigger(TICKER_SPEC)
        .low(ThresholdAndJob.create(LOW_PRICE, job1))
        .build();
    OneCancelsOtherProcessor processor = createProcessor(job);
    assertFailed(processor.start());
    verify(notificationService).error(Mockito.anyString());
  }

  private OneCancelsOtherProcessor createProcessor(OneCancelsOther job) {
    return new OneCancelsOtherProcessor(job, jobControl, enqueuer, statusUpdateService, notificationService, exchangeEventRegistry, exchangeService);
  }

  private void verifyDidNothingElse() {
    verify(exchangeService).exchangeSupportsPair(EXCHANGE, new CurrencyPair(BASE, COUNTER));
    verifyNoMoreInteractions(exchangeEventRegistry, notificationService, enqueuer, jobControl, exchangeService);
  }

  private void assertRunning(Status status) {
    Assert.assertEquals(Status.RUNNING, status);
  }

  private void assertFailed(Status status) {
    Assert.assertEquals(Status.FAILURE_PERMANENT, status);
  }
}