package com.grahamcrockford.oco.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.knowm.xchange.service.trade.params.orders.OrderQueryParamCurrencyPair;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.AsyncEventBus;
import com.grahamcrockford.oco.exchange.TradeServiceFactory;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.KeepAliveEvent;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.telegram.TelegramService;


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

  @Mock private TelegramService telegramService;

  @Mock private TradeServiceFactory tradeServiceFactory;
  @Mock private TradeService tradeService;
  @Mock private JobControl jobControl;
  @Mock private AsyncEventBus asyncEventBus;

  @Captor private ArgumentCaptor<Consumer<Ticker>> tickerConsumerCaptor;

  @Before
  public void before() throws IOException {
    MockitoAnnotations.initMocks(this);
    when(tradeServiceFactory.getForExchange(EXCHANGE)).thenReturn(tradeService);
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testNotSupportedByExchange() throws Exception {
    whenGetOrder().thenThrow(new NotAvailableFromExchangeException());
    whenGetOrders().thenThrow(new NotAvailableFromExchangeException());

    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    assertFalse(processor.start());
    verifyGotOrder();
    verifyGotOrders();
    verifySentMessage();
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
  }

  @Test
  public void testNotFoundStartup1() throws Exception {
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    assertFalse(processor.start());
    verifyGotOrder();
    verifySentMessage();
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
  }

  @Test
  public void testFallbackNothingFound() throws Exception {
    whenGetOrder().thenThrow(new NotAvailableFromExchangeException());
    whenGetOrders().thenReturn(new OpenOrders(ImmutableList.of()));

    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    assertFalse(processor.start());
    verifyGotOrder();
    verifyGotOrders();
    verifySentMessage();
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
  }

  @Test
  public void testFallbackNoMatchingFound() throws Exception {
    whenGetOrder().thenThrow(new NotAvailableFromExchangeException());

    LimitOrder order = mock(LimitOrder.class);
    when(order.getId()).thenReturn("NOTTHISONE");
    whenGetOrders().thenReturn(new OpenOrders(ImmutableList.of(order)));

    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    assertFalse(processor.start());
    verifyGotOrder();
    verifyGotOrders();
    verifySentMessage();
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
  }

  @Test
  public void testNotFoundPoll1() throws Exception {
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    returnOrder(Order.OrderStatus.NEW);
    assertTrue(processor.start());
    verifyGotOrder();
    verify(asyncEventBus).register(processor);
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);

    whenGetOrder().thenReturn(null);
    processor.process(KeepAliveEvent.INSTANCE);

    verifySentMessage();
    verify(jobControl).finish();
  }


  @Test
  public void testNotFoundStartup2() throws Exception {
    whenGetOrder().thenReturn(Collections.emptyList());
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    assertFalse(processor.start());
    verifyGotOrder();
    verifySentMessage();
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
  }

  @Test
  public void testNotFoundPoll2() throws Exception {
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    returnOrder(Order.OrderStatus.NEW);
    assertTrue(processor.start());
    verifyGotOrder();
    verify(asyncEventBus).register(processor);
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);

    whenGetOrder().thenReturn(Collections.emptyList());
    processor.process(KeepAliveEvent.INSTANCE);

    verifySentMessage();
    verify(jobControl).finish();
  }

  @Test
  public void testNotUniqueStartup() throws Exception {
    whenGetOrder().thenReturn(ImmutableList.of(mock(Order.class), mock(Order.class)));
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    assertFalse(processor.start());
    verifyGotOrder();
    verifySentMessage();
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
  }

  @Test
  public void testFallbackNotUnique() throws Exception {
    OrderStateNotifier job = baseJob().build();

    whenGetOrder().thenThrow(new NotAvailableFromExchangeException());

    LimitOrder order1 = mock(LimitOrder.class);
    LimitOrder order2 = mock(LimitOrder.class);
    when(order1.getId()).thenReturn(job.orderId());
    when(order2.getId()).thenReturn(job.orderId());
    whenGetOrders().thenReturn(new OpenOrders(ImmutableList.of(order1, order2)));

    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(job, jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    assertFalse(processor.start());
    verifyGotOrder();
    verifyGotOrders();
    verifySentMessage();
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
  }

  @Test
  public void testNotUniquePoll() throws Exception {
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    returnOrder(Order.OrderStatus.NEW);
    assertTrue(processor.start());
    verifyGotOrder();
    verify(asyncEventBus).register(processor);
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);

    whenGetOrder().thenReturn(ImmutableList.of(mock(Order.class), mock(Order.class)));
    processor.process(KeepAliveEvent.INSTANCE);

    verifySentMessage();
    verify(jobControl).finish();
  }

  @Test
  public void testStatusesStartup() throws Exception {
    for (final Order.OrderStatus status : Order.OrderStatus.values()) {
      Mockito.reset(telegramService, tradeService);
      returnOrder(status);
      OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
      boolean runningAsync = processor.start();
      if (ImmutableSet.of(Order.OrderStatus.PENDING_NEW, Order.OrderStatus.NEW, Order.OrderStatus.PARTIALLY_FILLED).contains(status)) {
        assertTrue(runningAsync);
        verifyGotOrder();
        verify(asyncEventBus).register(processor);
        verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
      } else {
        assertFalse(runningAsync);
        verifyGotOrder();
        verifySentMessage();
        verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
      }
    }
  }

  @Test
  public void testFallbackStatuses() throws Exception {
    OrderStateNotifier job = baseJob().build();

    LimitOrder order = mock(LimitOrder.class);
    when(order.getId()).thenReturn(job.orderId());

    for (final Order.OrderStatus status : Order.OrderStatus.values()) {
      Mockito.reset(telegramService, tradeService);
      whenGetOrder().thenThrow(new NotAvailableFromExchangeException());
      whenGetOrders().thenReturn(new OpenOrders(ImmutableList.of(order)));
      when(order.getStatus()).thenReturn(status);
      OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(job, jobControl, telegramService, tradeServiceFactory, asyncEventBus);
      boolean runningAsync = processor.start();
      if (ImmutableSet.of(Order.OrderStatus.PENDING_NEW, Order.OrderStatus.NEW, Order.OrderStatus.PARTIALLY_FILLED).contains(status)) {
        assertTrue(runningAsync);
        verifyGotOrder();
        verifyGotOrders();
        verify(asyncEventBus).register(processor);
        verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
      } else {
        assertFalse(runningAsync);
        verifyGotOrder();
        verifyGotOrders();
        verifySentMessage();
        verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
      }
    }
  }

  private void returnOrder(final Order.OrderStatus status) throws IOException {
    whenGetOrder().thenReturn(ImmutableList.of(new LimitOrder(ASK, AMOUNT, CURRENCY_PAIR, ORDER_ID, new Date(), LIMIT_PRICE, AVERAGE_PRICE, FILLED, BigDecimal.ZERO, status)));
  }


  /* ---------------------------------- Utility methods  ---------------------------------------------------- */

  private OngoingStubbing<Collection<Order>> whenGetOrder() throws IOException {
    return when(tradeService.getOrder(Matchers.any(OrderQueryParamCurrencyPair.class)));
  }

  private OngoingStubbing<OpenOrders> whenGetOrders() throws IOException {
    return when(tradeService.getOpenOrders(Matchers.any(OpenOrdersParams.class)));
  }

  private void verifyGotOrder() throws IOException {
    ArgumentCaptor<OrderQueryParamCurrencyPair> captor = ArgumentCaptor.forClass(OrderQueryParamCurrencyPair.class);
    verify(tradeService).getOrder(captor.capture());
    assertEquals(new CurrencyPair(BASE, COUNTER), captor.getValue().getCurrencyPair());
    assertEquals(ORDER_ID, captor.getValue().getOrderId());
  }

  private void verifyGotOrders() throws IOException {
    ArgumentCaptor<OpenOrdersParamCurrencyPair> captor = ArgumentCaptor.forClass(OpenOrdersParamCurrencyPair.class);
    verify(tradeService).getOpenOrders(captor.capture());
    assertEquals(new CurrencyPair(BASE, COUNTER), captor.getValue().getCurrencyPair());
  }

  private void verifySentMessage() {
    verify(telegramService).sendMessage(Mockito.anyString());
  }

  private OrderStateNotifier.Builder baseJob() {
    return OrderStateNotifier.builder()
      .id(JOB_ID)
      .orderId(ORDER_ID)
      .tickTrigger(TickerSpec.builder()
        .base(BASE)
        .counter(COUNTER)
        .exchange(EXCHANGE)
        .build()
      );
  }
}