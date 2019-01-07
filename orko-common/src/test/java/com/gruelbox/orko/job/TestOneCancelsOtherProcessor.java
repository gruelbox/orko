/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gruelbox.orko.job;

import static com.gruelbox.orko.db.MockTransactionallyFactory.mockTransactionally;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;

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

import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.job.OneCancelsOther.ThresholdAndJob;
import com.gruelbox.orko.jobrun.JobSubmitter;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.jobrun.spi.StatusUpdateService;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.marketdata.MarketDataSubscription;
import com.gruelbox.orko.marketdata.TickerEvent;
import com.gruelbox.orko.notification.Notification;
import com.gruelbox.orko.notification.NotificationLevel;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;

import io.reactivex.Flowable;

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
  @Mock private ExchangeEventSubscription subscription;

  @Mock private Job job1;
  @Mock private Job job2;

  @Captor private ArgumentCaptor<io.reactivex.functions.Consumer<? super TickerEvent>> tickerConsumerCaptor;
  private Flowable<TickerEvent> tickerData;

  @Before
  public void before() throws IOException {
    MockitoAnnotations.initMocks(this);
    when(exchangeService.exchangeSupportsPair(EXCHANGE, new CurrencyPair(BASE, COUNTER))).thenReturn(true);
    when(exchangeEventRegistry.subscribe(Mockito.any(MarketDataSubscription.class)))
      .thenReturn(subscription);
    when(subscription.getTickers()).thenAnswer((args) -> tickerData);
  }


  @Test
  public void testAtLowNoJob() throws Exception {
    OneCancelsOther job = OneCancelsOther.builder()
        .id(JOB_ID)
        .tickTrigger(TICKER_SPEC)
        .high(ThresholdAndJob.create(HIGH_PRICE, job2))
        .build();

    tickerData = Flowable.just(
      TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
        .bid(LOW_PRICE)
        .build())
    );

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verifySubscribed(job);
    verifyGotTickers();
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

    tickerData = Flowable.just(
        TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
          .bid(LOW_PRICE)
          .build())
      );

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verifySubscribed(job);
    verifyGotTickers();

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

    tickerData = Flowable.just(
        TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
          .bid(LOW_PRICE)
          .build())
      );

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verifySubscribed(job);
    verifyGotTickers();

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

    tickerData = Flowable.just(
      TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
        .bid(LOW_PRICE.add(BigDecimal.ONE))
        .build())
    );

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verifySubscribed(job);
    verifyGotTickers();
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

    tickerData = Flowable.just(
      TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
        .bid(HIGH_PRICE.subtract(BigDecimal.ONE))
        .build())
    );

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verifySubscribed(job);
    verifyGotTickers();
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

    tickerData = Flowable.just(
      TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
        .bid(HIGH_PRICE)
        .build())
    );

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verifySubscribed(job);
    verifyGotTickers();

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

    tickerData = Flowable.just(
      TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
        .bid(HIGH_PRICE)
        .build())
    );

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verifySubscribed(job);
    verifyGotTickers();

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

    tickerData = Flowable.just(
      TickerEvent.create(job.tickTrigger(), new Ticker.Builder()
        .bid(HIGH_PRICE)
        .build())
    );

    OneCancelsOtherProcessor processor = createProcessor(job);
    assertRunning(processor.start());
    verifySubscribed(job);
    verifyGotTickers();
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
    return new OneCancelsOtherProcessor(job, jobControl, enqueuer, statusUpdateService,
        notificationService, exchangeEventRegistry, exchangeService, mockTransactionally());
  }

  private void verifyDidNothingElse() {
    verify(exchangeService).exchangeSupportsPair(EXCHANGE, new CurrencyPair(BASE, COUNTER));
    verifyNoMoreInteractions(exchangeEventRegistry, notificationService, enqueuer, jobControl, exchangeService, subscription);
  }

  private void verifyGotTickers() {
    verify(subscription).getTickers();
  }

  private void verifySubscribed(OneCancelsOther job) {
    verify(exchangeEventRegistry).subscribe(MarketDataSubscription.create(job.tickTrigger(), TICKER));
  }

  private void assertRunning(Status status) {
    Assert.assertEquals(Status.RUNNING, status);
  }

  private void assertFailed(Status status) {
    Assert.assertEquals(Status.FAILURE_PERMANENT, status);
  }
}