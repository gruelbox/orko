package com.grahamcrockford.oco.job;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.google.common.collect.ImmutableList;
import com.grahamcrockford.oco.marketdata.ExchangeEventRegistry;
import com.grahamcrockford.oco.marketdata.MarketDataSubscription;
import com.grahamcrockford.oco.marketdata.MarketDataType;
import com.grahamcrockford.oco.marketdata.OpenOrdersEvent;
import com.grahamcrockford.oco.notification.NotificationService;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.TickerSpec;

import io.reactivex.Flowable;


public class TestOrderStateNotifierProcessor {

  private static final String JOB_ID = "555";

  private static final String BASE = "FOO";
  private static final String COUNTER = "USDT";
  private static final CurrencyPair CURRENCY_PAIR = new CurrencyPair(BASE, COUNTER);
  private static final String EXCHANGE = "fooex";
  private static final String ORDER_ID = "65734-232131";

  private static final BigDecimal AMOUNT = new BigDecimal(1000);
  private static final BigDecimal AVERAGE_PRICE = new BigDecimal(95);
  private static final BigDecimal LIMIT_PRICE = new BigDecimal(90);
  private static final BigDecimal FILLED = new BigDecimal(999);

  @Mock private NotificationService notificationService;
  @Mock private JobControl jobControl;
  @Mock private ExchangeEventRegistry exchangeEventRegistry;

  @Captor private ArgumentCaptor<Consumer<Ticker>> tickerConsumerCaptor;

  @Before
  public void before() throws IOException {
    MockitoAnnotations.initMocks(this);
    doAnswer(a -> {
      System.out.println(Arrays.toString(a.getArguments()));
      return null;
    }).when(notificationService).info(Mockito.anyString());
    doAnswer(a -> {
      System.out.println(Arrays.toString(a.getArguments()));
      return null;
    }).when(notificationService).error(Mockito.anyString());
    doAnswer(a -> {
      System.out.println(Arrays.toString(a.getArguments()));
      return null;
    }).when(notificationService).error(Mockito.anyString(), Mockito.any(Throwable.class));
  }

  @Test
  public void testNothingFound() throws Exception {

    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, notificationService, exchangeEventRegistry);

    when(exchangeEventRegistry.getOpenOrders(Mockito.anyString()))
      .thenReturn(Flowable.just(OpenOrdersEvent.create(ticker(), new OpenOrders(ImmutableList.of()))));

    assertTrue(processor.start());

    verifyConnected();
    verifySentMessage();
    verifyFinished();
    verifyNoMoreInteractions(jobControl, notificationService, exchangeEventRegistry);
  }

  @Test
  public void testNoMatchingFound() throws Exception {
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, notificationService, exchangeEventRegistry);

    LimitOrder order = mock(LimitOrder.class);
    when(order.getId()).thenReturn("NOTTHISONE");
    when(exchangeEventRegistry.getOpenOrders(Mockito.anyString()))
      .thenReturn(Flowable.just(OpenOrdersEvent.create(ticker(), new OpenOrders(ImmutableList.of(order)))));

    assertTrue(processor.start());

    verifyConnected();
    verifySentMessage();
    verifyFinished();
    verifyNoMoreInteractions(jobControl, notificationService, exchangeEventRegistry);
  }

  @Test
  public void testNotUnique() throws Exception {
    OrderStateNotifier job = baseJob().build();
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(job, jobControl, notificationService, exchangeEventRegistry);

    LimitOrder order1 = mock(LimitOrder.class);
    LimitOrder order2 = mock(LimitOrder.class);
    when(order1.getId()).thenReturn(job.orderId());
    when(order2.getId()).thenReturn(job.orderId());
    when(exchangeEventRegistry.getOpenOrders(Mockito.anyString()))
      .thenReturn(Flowable.just(OpenOrdersEvent.create(ticker(), new OpenOrders(ImmutableList.of(order1, order2)))));

    assertTrue(processor.start());

    verifyConnected();
    verifySentError();
    verifyFinished();
    verifyNoMoreInteractions(jobControl, notificationService, exchangeEventRegistry);
  }

  @Test
  public void testFound() throws Exception {
    OrderStateNotifier job = baseJob().build();
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(job, jobControl, notificationService, exchangeEventRegistry);

    LimitOrder order = mock(LimitOrder.class);
    when(order.getId()).thenReturn(ORDER_ID);
    when(exchangeEventRegistry.getOpenOrders(Mockito.anyString()))
      .thenReturn(Flowable.just(OpenOrdersEvent.create(ticker(), new OpenOrders(ImmutableList.of(order)))));

    assertTrue(processor.start());

    verifyConnected();
    verifyNoMoreInteractions(jobControl, notificationService, exchangeEventRegistry);
  }

  @Test
  public void testFoundSecondAttempt() throws Exception {
    OrderStateNotifier job = baseJob().build();
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(job, jobControl, notificationService, exchangeEventRegistry);

    LimitOrder order = mock(LimitOrder.class);
    when(order.getId()).thenReturn(ORDER_ID);
    when(exchangeEventRegistry.getOpenOrders(Mockito.anyString()))
      .thenReturn(Flowable.just(
          OpenOrdersEvent.create(ticker(), new OpenOrders(ImmutableList.of(order))),
          OpenOrdersEvent.create(ticker(), new OpenOrders(ImmutableList.of(order)))
      ));

    assertTrue(processor.start());

    verifyConnected();
    verifyNoMoreInteractions(jobControl, notificationService, exchangeEventRegistry);
  }

  @Test
  public void testGoneOnSecondAttempt() throws Exception {
    OrderStateNotifier job = baseJob().build();
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(job, jobControl, notificationService, exchangeEventRegistry);

    LimitOrder order = mock(LimitOrder.class);
    when(order.getId()).thenReturn(ORDER_ID);
    when(order.getOriginalAmount()).thenReturn(new BigDecimal(100));
    when(exchangeEventRegistry.getOpenOrders(Mockito.anyString()))
      .thenReturn(Flowable.just(
          OpenOrdersEvent.create(ticker(), new OpenOrders(ImmutableList.of(order))),
          OpenOrdersEvent.create(ticker(), new OpenOrders(ImmutableList.of()))
      ));

    assertTrue(processor.start());

    verifyConnected();
    verifySentMessage();
    verifyFinished();
    verifyNoMoreInteractions(jobControl, notificationService, exchangeEventRegistry);
  }


  /* ---------------------------------- Utility methods  ---------------------------------------------------- */

  private void verifyFinished() {
    verify(jobControl).finish();
  }

  private void verifyConnected() {
    verify(exchangeEventRegistry).changeSubscriptions(Mockito.anyString(), Mockito.eq(MarketDataSubscription.create(ticker(), MarketDataType.OPEN_ORDERS)));
    verify(exchangeEventRegistry).getOpenOrders(Mockito.anyString());
  }

  private void verifySentError() {
    verify(notificationService).error(Mockito.anyString());
  }

  private void verifySentMessage() {
    verify(notificationService).info(Mockito.anyString());
  }

  private OrderStateNotifier.Builder baseJob() {
    return OrderStateNotifier.builder()
      .id(JOB_ID)
      .orderId(ORDER_ID)
      .tickTrigger(ticker());
  }

  private TickerSpec ticker() {
    return TickerSpec.builder()
      .base(BASE)
      .counter(COUNTER)
      .exchange(EXCHANGE)
      .build();
  }
}