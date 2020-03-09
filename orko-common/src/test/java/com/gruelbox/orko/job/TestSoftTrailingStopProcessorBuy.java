/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.job;

import static com.gruelbox.orko.db.MockTransactionallyFactory.mockTransactionally;
import static com.gruelbox.orko.exchange.MarketDataType.TICKER;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.gruelbox.orko.exchange.ExchangeEventRegistry;
import com.gruelbox.orko.exchange.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.exchange.MarketDataSubscription;
import com.gruelbox.orko.exchange.TickerEvent;
import com.gruelbox.orko.job.LimitOrderJob.BalanceState;
import com.gruelbox.orko.job.LimitOrderJob.Direction;
import com.gruelbox.orko.job.SoftTrailingStop.Builder;
import com.gruelbox.orko.jobrun.JobSubmitter;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.jobrun.spi.StatusUpdateService;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;
import io.reactivex.Flowable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
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

public class TestSoftTrailingStopProcessorBuy {

  private static final int PRICE_SCALE = 2;
  private static final BigDecimal HUNDRED = new BigDecimal(100);
  private static final BigDecimal PENNY = new BigDecimal("0.01");

  private static final BigDecimal AMOUNT = new BigDecimal(1000);

  private static final BigDecimal ENTRY_PRICE = HUNDRED;
  private static final BigDecimal STOP_PRICE =
      ENTRY_PRICE.add(new BigDecimal(RandomUtils.nextInt(2, 10)));
  private static final BigDecimal LIMIT_PRICE =
      STOP_PRICE.add(new BigDecimal(RandomUtils.nextInt(2, 10)));

  private static final BigDecimal LOWER_ENTRY_PRICE =
      ENTRY_PRICE.subtract(new BigDecimal(RandomUtils.nextInt(2, 50)));
  private static final BigDecimal ADJUSTED_STOP_PRICE =
      STOP_PRICE.subtract(ENTRY_PRICE).add(LOWER_ENTRY_PRICE);

  private static final String BASE = "FOO";
  private static final String COUNTER = "USDT";
  private static final String EXCHANGE = "fooex";

  @Mock private JobSubmitter jobSubmitter;
  @Mock private StatusUpdateService statusUpdateService;
  @Mock private NotificationService notificationService;
  @Mock private ExchangeService exchangeService;

  @Mock private Exchange exchange;
  @Mock private MarketDataService marketDataService;
  @Mock private JobControl jobControl;
  @Mock private CurrencyPairMetaData currencyPairMetaData;
  @Mock private ExchangeEventRegistry exchangeEventRegistry;
  @Mock private ExchangeEventSubscription subscription;

  @Captor private ArgumentCaptor<Consumer<Ticker>> tickerConsumerCaptor;
  private Flowable<TickerEvent> tickerData;

  @Before
  public void before() throws IOException {
    MockitoAnnotations.initMocks(this);
    when(currencyPairMetaData.getPriceScale()).thenReturn(PRICE_SCALE);
    when(exchangeEventRegistry.subscribe(Mockito.any(MarketDataSubscription.class)))
        .thenReturn(subscription);
    when(subscription.getTickers()).thenAnswer((args) -> tickerData);
  }

  @Test
  public void testErrNoBuyers() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    tickerData =
        Flowable.just(
            TickerEvent.create(
                job.tickTrigger(),
                new Ticker.Builder()
                    .last(ENTRY_PRICE)
                    .ask(ENTRY_PRICE.add(PENNY))
                    .timestamp(new Date())
                    .build()));

    start(job, processor);

    verifySentError();
    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testErrNoSellers() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    tickerData =
        Flowable.just(
            TickerEvent.create(
                job.tickTrigger(),
                new Ticker.Builder()
                    .last(ENTRY_PRICE)
                    .ask(ENTRY_PRICE.add(PENNY))
                    .timestamp(new Date())
                    .build()));

    start(job, processor);

    verifySentError();
    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testActiveStopFalseDefault1() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    tickerData =
        Flowable.just(
            TickerEvent.create(job.tickTrigger(), everythingAt(STOP_PRICE.subtract(PENNY))));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueDefault1() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    Ticker ticker = everythingAt(STOP_PRICE);
    tickerData = Flowable.just(TickerEvent.create(job.tickTrigger(), ticker));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyLimitBuyAtLimitPrice(ticker);
    verifyFinished();
    verifySentMessage();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseDefault2() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    tickerData =
        Flowable.just(
            TickerEvent.create(
                job.tickTrigger(),
                new Ticker.Builder()
                    .bid(STOP_PRICE.subtract(PENNY).subtract(PENNY))
                    .last(STOP_PRICE)
                    .ask(STOP_PRICE.subtract(PENNY))
                    .timestamp(new Date())
                    .build()));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueDefault2() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    final Ticker ticker =
        new Ticker.Builder()
            .bid(STOP_PRICE.subtract(PENNY).subtract(PENNY))
            .last(STOP_PRICE)
            .ask(STOP_PRICE)
            .timestamp(new Date())
            .build();
    tickerData = Flowable.just(TickerEvent.create(job.tickTrigger(), ticker));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyLimitBuyAtLimitPrice(ticker);
    verifyFinished();
    verifySentMessage();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseAdjusted1() throws Exception {
    SoftTrailingStop job = jobAlreadyAdjusted().build();
    SoftTrailingStopProcessor processor = processor(job);

    final Ticker ticker = everythingAt(ADJUSTED_STOP_PRICE.subtract(PENNY));
    tickerData = Flowable.just(TickerEvent.create(job.tickTrigger(), ticker));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueAdjusted1() throws Exception {
    SoftTrailingStop job = jobAlreadyAdjusted().build();
    SoftTrailingStopProcessor processor = processor(job);

    final Ticker ticker = everythingAt(ADJUSTED_STOP_PRICE);
    tickerData = Flowable.just(TickerEvent.create(job.tickTrigger(), ticker));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyLimitBuyAtLimitPrice(ticker);
    verifyFinished();
    verifySentMessage();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseAdjusted2() throws Exception {
    SoftTrailingStop job = jobAlreadyAdjusted().build();
    SoftTrailingStopProcessor processor = processor(job);

    final Ticker ticker =
        new Ticker.Builder()
            .bid(ADJUSTED_STOP_PRICE.subtract(PENNY).subtract(PENNY))
            .last(ADJUSTED_STOP_PRICE)
            .ask(ADJUSTED_STOP_PRICE.subtract(PENNY))
            .timestamp(new Date())
            .build();
    tickerData = Flowable.just(TickerEvent.create(job.tickTrigger(), ticker));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueAdjusted2() throws Exception {
    SoftTrailingStop job = jobAlreadyAdjusted().build();
    SoftTrailingStopProcessor processor = processor(job);

    final Ticker ticker =
        new Ticker.Builder()
            .bid(ADJUSTED_STOP_PRICE.subtract(PENNY).subtract(PENNY))
            .last(ADJUSTED_STOP_PRICE)
            .ask(ADJUSTED_STOP_PRICE)
            .timestamp(new Date())
            .build();
    tickerData = Flowable.just(TickerEvent.create(job.tickTrigger(), ticker));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyLimitBuyAtLimitPrice(ticker);
    verifyFinished();
    verifySentMessage();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopFalseRounding() throws Exception {
    final SoftTrailingStop job =
        baseJob().stopPrice(new BigDecimal("156.675")).limitPrice(new BigDecimal("160")).build();
    SoftTrailingStopProcessor processor = processor(job);

    final Ticker ticker = everythingAt(new BigDecimal("156.67"));
    tickerData = Flowable.just(TickerEvent.create(job.tickTrigger(), ticker));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveStopTrueRounding() throws Exception {
    final SoftTrailingStop job =
        baseJob().stopPrice(new BigDecimal("156.675")).limitPrice(new BigDecimal("160")).build();
    SoftTrailingStopProcessor processor = processor(job);

    final Ticker ticker = everythingAt(new BigDecimal("156.68"));
    tickerData = Flowable.just(TickerEvent.create(job.tickTrigger(), ticker));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyLimitBuyAt(ticker, new BigDecimal("160"));
    verifyFinished();
    verifySentMessage();
    verifyDidNothingElse();
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testActiveAdjustNoChange() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    final Ticker ticker = everythingAt(ENTRY_PRICE);
    tickerData = Flowable.just(TickerEvent.create(job.tickTrigger(), ticker));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveAdjustPriceRise() throws Exception {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    final Ticker ticker = everythingAt(ENTRY_PRICE.add(PENNY));
    tickerData = Flowable.just(TickerEvent.create(job.tickTrigger(), ticker));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveAdjustPriceDrop() throws Exception {
    final SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    final Ticker ticker =
        new Ticker.Builder()
            .bid(ENTRY_PRICE.subtract(PENNY).subtract(PENNY))
            .last(ENTRY_PRICE)
            .ask(ENTRY_PRICE.subtract(PENNY))
            .timestamp(new Date())
            .build();
    tickerData = Flowable.just(TickerEvent.create(job.tickTrigger(), ticker));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyResyncedPriceTo(job, ENTRY_PRICE.subtract(PENNY));
    verifyDidNothingElse();
  }

  @Test
  public void testActiveAdjustSecondTimeNoChange() throws Exception {
    SoftTrailingStop job = jobAlreadyAdjusted().build();
    SoftTrailingStopProcessor processor = processor(job);

    final Ticker ticker = everythingAt(LOWER_ENTRY_PRICE);
    tickerData = Flowable.just(TickerEvent.create(job.tickTrigger(), ticker));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveAdjustSecondTimePriceRise() throws Exception {
    SoftTrailingStop job = jobAlreadyAdjusted().build();
    SoftTrailingStopProcessor processor = processor(job);

    final Ticker ticker = everythingAt(LOWER_ENTRY_PRICE.add(PENNY));
    tickerData = Flowable.just(TickerEvent.create(job.tickTrigger(), ticker));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyWillRepeatWithoutChange();
    verifyDidNothingElse();
  }

  @Test
  public void testActiveAdjustSecondTimePriceDrop() throws Exception {
    final SoftTrailingStop job = jobAlreadyAdjusted().build();
    SoftTrailingStopProcessor processor = processor(job);

    final Ticker ticker =
        new Ticker.Builder()
            .bid(LOWER_ENTRY_PRICE.subtract(PENNY).subtract(PENNY))
            .last(LOWER_ENTRY_PRICE)
            .ask(LOWER_ENTRY_PRICE.subtract(PENNY))
            .timestamp(new Date())
            .build();
    tickerData = Flowable.just(TickerEvent.create(job.tickTrigger(), ticker));

    start(job, processor);

    verifyValidatedABunchOfTimes();
    verifyResyncedPriceTo(job, LOWER_ENTRY_PRICE.subtract(PENNY));
    verifyDidNothingElse();
  }

  @Test
  public void testValidateSwitchToInsufficient() {
    SoftTrailingStop job = baseJob().build();
    SoftTrailingStopProcessor processor = processor(job);

    doAnswer(
            inv -> {
              LimitOrderJob limitOrderJob = inv.getArgument(0, LimitOrderJob.class);
              inv.getArgument(1, JobControl.class)
                  .replace(
                      limitOrderJob
                          .toBuilder()
                          .balanceState(BalanceState.INSUFFICIENT_BALANCE)
                          .build());
              return null;
            })
        .when(jobSubmitter)
        .validate(Mockito.any(Job.class), Mockito.any(JobControl.class));

    doAnswer(
            inv -> {
              processor.setReplacedJob(inv.getArgument(0, SoftTrailingStop.class));
              return null;
            })
        .when(jobControl)
        .replace(Mockito.any(Job.class));

    tickerData =
        Flowable.just(
            TickerEvent.create(
                job.tickTrigger(),
                new Ticker.Builder()
                    .bid(STOP_PRICE.subtract(PENNY).subtract(PENNY))
                    .last(STOP_PRICE)
                    .ask(STOP_PRICE.subtract(PENNY))
                    .timestamp(new Date())
                    .build()));

    start(job, processor);

    ArgumentCaptor<LimitOrderJob> limitOrderJobCaptor =
        ArgumentCaptor.forClass(LimitOrderJob.class);
    verify(jobSubmitter).validate(limitOrderJobCaptor.capture(), Mockito.any(JobControl.class));
    assertThat(limitOrderJobCaptor.getValue().amount(), equalTo(AMOUNT));
    assertThat(limitOrderJobCaptor.getValue().direction(), equalTo(Direction.BUY));
    assertThat(limitOrderJobCaptor.getValue().limitPrice(), equalTo(LIMIT_PRICE));
    assertThat(
        limitOrderJobCaptor.getValue().balanceState(), equalTo(BalanceState.SUFFICIENT_BALANCE));

    ArgumentCaptor<Job> replacedJobCaptor = ArgumentCaptor.forClass(Job.class);
    verify(jobControl).replace(replacedJobCaptor.capture());
    assertThat(
        replacedJobCaptor.getValue(),
        equalTo(job.toBuilder().balanceState(BalanceState.INSUFFICIENT_BALANCE).build()));
  }

  @Test
  public void testValidateSwitchToSufficient() {
    SoftTrailingStop job = baseJob().balanceState(BalanceState.INSUFFICIENT_BALANCE).build();
    SoftTrailingStopProcessor processor = processor(job);

    doAnswer(
            inv -> {
              LimitOrderJob limitOrderJob = inv.getArgument(0, LimitOrderJob.class);
              inv.getArgument(1, JobControl.class)
                  .replace(
                      limitOrderJob
                          .toBuilder()
                          .balanceState(BalanceState.SUFFICIENT_BALANCE)
                          .build());
              return null;
            })
        .when(jobSubmitter)
        .validate(Mockito.any(Job.class), Mockito.any(JobControl.class));

    doAnswer(
            inv -> {
              processor.setReplacedJob(inv.getArgument(0, SoftTrailingStop.class));
              return null;
            })
        .when(jobControl)
        .replace(Mockito.any(Job.class));

    tickerData =
        Flowable.just(
            TickerEvent.create(
                job.tickTrigger(),
                new Ticker.Builder()
                    .bid(STOP_PRICE.subtract(PENNY).subtract(PENNY))
                    .last(STOP_PRICE)
                    .ask(STOP_PRICE.subtract(PENNY))
                    .timestamp(new Date())
                    .build()));

    start(job, processor);

    ArgumentCaptor<LimitOrderJob> limitOrderJobCaptor =
        ArgumentCaptor.forClass(LimitOrderJob.class);
    verify(jobSubmitter).validate(limitOrderJobCaptor.capture(), Mockito.any(JobControl.class));
    assertThat(limitOrderJobCaptor.getValue().amount(), equalTo(AMOUNT));
    assertThat(limitOrderJobCaptor.getValue().direction(), equalTo(Direction.BUY));
    assertThat(limitOrderJobCaptor.getValue().limitPrice(), equalTo(LIMIT_PRICE));
    assertThat(
        limitOrderJobCaptor.getValue().balanceState(), equalTo(BalanceState.INSUFFICIENT_BALANCE));

    ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
    verify(jobControl).replace(captor.capture());
    assertThat(
        captor.getValue(),
        equalTo(job.toBuilder().balanceState(BalanceState.SUFFICIENT_BALANCE).build()));
  }

  /* ---------------------------------- Utility methods  ---------------------------------------------------- */

  private void start(SoftTrailingStop job, SoftTrailingStopProcessor processor) {
    assertRunning(processor.start());
    verifySubscribed(job);
    verifyGotTickers();
  }

  private void verifyGotTickers() {
    verify(subscription).getTickers();
  }

  private void verifySubscribed(SoftTrailingStop job) {
    verify(exchangeEventRegistry)
        .subscribe(MarketDataSubscription.create(job.tickTrigger(), TICKER));
  }

  private void assertRunning(Status status) {
    Assert.assertEquals(Status.RUNNING, status);
  }

  private SoftTrailingStopProcessor processor(SoftTrailingStop job) {
    return new SoftTrailingStopProcessor(
        job,
        jobControl,
        statusUpdateService,
        notificationService,
        exchangeService,
        jobSubmitter,
        exchangeEventRegistry,
        mockTransactionally());
  }

  private void verifyResyncedPriceTo(SoftTrailingStop job, BigDecimal syncPrice)
      throws IOException {
    verify(jobControl)
        .replace(
            job.toBuilder()
                .lastSyncPrice(syncPrice)
                .stopPrice(job.stopPrice().add(syncPrice).subtract(job.lastSyncPrice()))
                .build());
  }

  private void verifyWillRepeatWithoutChange() {
    verifyNoInteractions(jobControl);
  }

  private void verifyDidNothingElse() {
    verifyNoMoreInteractions(notificationService, jobSubmitter);
  }

  private void verifySentMessage() {
    verify(notificationService).info(Mockito.anyString());
  }

  private void verifySentError() {
    verify(notificationService).error(Mockito.anyString());
  }

  private void verifyLimitBuyAtLimitPrice(final Ticker ticker) throws Exception {
    verifyLimitBuyAt(ticker, LIMIT_PRICE);
  }

  private void verifyLimitBuyAt(final Ticker ticker, BigDecimal price) throws Exception {
    verify(jobSubmitter)
        .submitNewUnchecked(
            LimitOrderJob.builder()
                .tickTrigger(
                    TickerSpec.builder().exchange(EXCHANGE).counter(COUNTER).base(BASE).build())
                .direction(Direction.BUY)
                .amount(AMOUNT)
                .limitPrice(price)
                .build());
  }

  private void verifyValidatedABunchOfTimes() {
    verify(jobSubmitter, atLeastOnce())
        .validate(Mockito.any(Job.class), Mockito.any(JobControl.class));
  }

  private void verifyFinished() {
    verify(jobControl).finish(Status.SUCCESS);
  }

  private Ticker everythingAt(BigDecimal price) {
    return new Ticker.Builder().bid(price).last(price).ask(price).timestamp(new Date()).build();
  }

  private SoftTrailingStop.Builder baseJob() {
    TickerSpec ex = TickerSpec.builder().base(BASE).counter(COUNTER).exchange(EXCHANGE).build();
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
