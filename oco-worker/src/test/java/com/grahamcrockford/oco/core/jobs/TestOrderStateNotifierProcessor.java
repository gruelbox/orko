package com.grahamcrockford.oco.core.jobs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.trade.TradeService;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.AsyncEventBus;
import com.grahamcrockford.oco.api.job.OrderStateNotifier;
import com.grahamcrockford.oco.core.TradeServiceFactory;
import com.grahamcrockford.oco.core.telegram.TelegramService;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.KeepAliveEvent;


public class TestOrderStateNotifierProcessor {

  private static final String JOB_ID = "555";

  private static final String BASE = "FOO";
  private static final String COUNTER = "USDT";
  private static final CurrencyPair CURRENCY_PAIR = new CurrencyPair(BASE, COUNTER);
  private static final String EXCHANGE = "fooex";
  private static final String DESCRIPTION = "OAIUHDFSDF";
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
    when(tradeService.getOrder(ORDER_ID)).thenThrow(new NotAvailableFromExchangeException());

    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    assertFalse(processor.start());
    verify(tradeService).getOrder(ORDER_ID);
    verifySentMessage();
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
  }

  @Test
  public void testNotFoundStartup1() throws Exception {
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    assertFalse(processor.start());
    verify(tradeService).getOrder(ORDER_ID);
    verifySentMessage();
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
  }

  @Test
  public void testNotFoundPoll1() throws Exception {
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    returnOrder(Order.OrderStatus.NEW);
    assertTrue(processor.start());
    verify(tradeService).getOrder(ORDER_ID);
    verify(asyncEventBus).register(processor);
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);

    when(tradeService.getOrder(ORDER_ID)).thenReturn(null);
    processor.process(KeepAliveEvent.INSTANCE);

    verifySentMessage();
    verify(jobControl).finish();
  }

  @Test
  public void testNotFoundStartup2() throws Exception {
    when(tradeService.getOrder(ORDER_ID)).thenReturn(Collections.emptyList());
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    assertFalse(processor.start());
    verify(tradeService).getOrder(ORDER_ID);
    verifySentMessage();
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
  }

  @Test
  public void testNotFoundPoll2() throws Exception {
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    returnOrder(Order.OrderStatus.NEW);
    assertTrue(processor.start());
    verify(tradeService).getOrder(ORDER_ID);
    verify(asyncEventBus).register(processor);
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);

    when(tradeService.getOrder(ORDER_ID)).thenReturn(Collections.emptyList());
    processor.process(KeepAliveEvent.INSTANCE);

    verifySentMessage();
    verify(jobControl).finish();
  }

  @Test
  public void testNotUniqueStartup() throws Exception {
    when(tradeService.getOrder(ORDER_ID)).thenReturn(ImmutableList.of(mock(Order.class), mock(Order.class)));
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    assertFalse(processor.start());
    verify(tradeService).getOrder(ORDER_ID);
    verifySentMessage();
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
  }

  @Test
  public void testNotUniquePoll() throws Exception {
    OrderStateNotifierProcessor processor = new OrderStateNotifierProcessor(baseJob().build(), jobControl, telegramService, tradeServiceFactory, asyncEventBus);
    returnOrder(Order.OrderStatus.NEW);
    assertTrue(processor.start());
    verify(tradeService).getOrder(ORDER_ID);
    verify(asyncEventBus).register(processor);
    verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);

    when(tradeService.getOrder(ORDER_ID)).thenReturn(ImmutableList.of(mock(Order.class), mock(Order.class)));
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
        verify(tradeService).getOrder(ORDER_ID);
        verify(asyncEventBus).register(processor);
        verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
      } else {
        assertFalse(runningAsync);
        verify(tradeService).getOrder(ORDER_ID);
        verifySentMessage();
        verifyNoMoreInteractions(jobControl, telegramService, tradeService, asyncEventBus);
      }
    }
  }

  private void returnOrder(final Order.OrderStatus status) throws IOException {
    when(tradeService.getOrder(ORDER_ID)).thenReturn(ImmutableList.of(new LimitOrder(ASK, AMOUNT, CURRENCY_PAIR, ORDER_ID, new Date(), LIMIT_PRICE, AVERAGE_PRICE, FILLED, BigDecimal.ZERO, status)));
  }

  /* ---------------------------------- Utility methods  ---------------------------------------------------- */

  private void verifySentMessage() {
    verify(telegramService).sendMessage(Mockito.anyString());
  }

  private OrderStateNotifier.Builder baseJob() {
    return OrderStateNotifier.builder()
      .id(JOB_ID)
      .description(DESCRIPTION)
      .orderId(ORDER_ID)
      .exchange(EXCHANGE);
  }
}