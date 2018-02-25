package com.grahamcrockford.oco.core.jobs;

import static org.junit.Assert.assertFalse;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.trade.TradeService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.grahamcrockford.oco.api.Job;
import com.grahamcrockford.oco.core.TradeServiceFactory;
import com.grahamcrockford.oco.telegram.TelegramService;
import com.grahamcrockford.oco.util.Sleep;


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
  @Mock private Sleep sleep;

  private OrderStateNotifierProcessor processor;

  @Before
  public void before() throws IOException {

    MockitoAnnotations.initMocks(this);

    when(tradeServiceFactory.getForExchange(EXCHANGE)).thenReturn(tradeService);

    processor = new OrderStateNotifierProcessor(telegramService, tradeServiceFactory, sleep);
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testNotSupportedByExchange() throws Exception {
    when(tradeService.getOrder(ORDER_ID)).thenThrow(new NotAvailableFromExchangeException());
    Optional<OrderStateNotifier> result = processor.process(baseJob().build());
    verifySentMessage();
    verifyFinishedJob(result);
  }

  @Test
  public void testNotFound1() throws Exception {
    Optional<OrderStateNotifier> result = processor.process(baseJob().build());
    verifySentMessage();
    verifyFinishedJob(result);
  }

  @Test
  public void testNotFound2() throws Exception {
    when(tradeService.getOrder(ORDER_ID)).thenReturn(Collections.emptyList());
    Optional<OrderStateNotifier> result = processor.process(baseJob().build());
    verifySentMessage();
    verifyFinishedJob(result);
  }

  @Test
  public void testNotUnique() throws Exception {
    when(tradeService.getOrder(ORDER_ID)).thenReturn(ImmutableList.of(mock(Order.class), mock(Order.class)));
    Optional<OrderStateNotifier> result = processor.process(baseJob().build());
    verifySentMessage();
    verifyFinishedJob(result);
  }

  @Test
  public void testStatuses() throws Exception {
    for (final Order.OrderStatus status : Order.OrderStatus.values()) {
      Mockito.reset(telegramService);
      when(tradeService.getOrder(ORDER_ID)).thenReturn(ImmutableList.of(new LimitOrder(ASK, AMOUNT, CURRENCY_PAIR, ORDER_ID, new Date(), LIMIT_PRICE, AVERAGE_PRICE, FILLED, BigDecimal.ZERO, status)));
      Optional<OrderStateNotifier> result = processor.process(baseJob().build());
      if (ImmutableSet.of(Order.OrderStatus.PENDING_NEW, Order.OrderStatus.NEW, Order.OrderStatus.PARTIALLY_FILLED).contains(status)) {
        verifyNoChanges(baseJob().build(), result);
      } else {
        verifySentMessage();
        verifyFinishedJob(result);
      }
    }
  }

  /* ---------------------------------- Utility methods  ---------------------------------------------------- */

  private void verifyNoChanges(Job order, Optional<OrderStateNotifier> result) {
    verifyZeroInteractions(telegramService);
    Assert.assertEquals(order, result.get());
  }

  private void verifySentMessage() {
    verify(telegramService).sendMessage(Mockito.anyString());
  }

  private void verifyFinishedJob(Optional<OrderStateNotifier> result) {
    assertFalse(result.isPresent());
  }

  private OrderStateNotifier.Builder baseJob() {
    return OrderStateNotifier.builder()
      .id(JOB_ID)
      .description(DESCRIPTION)
      .orderId(ORDER_ID)
      .exchange(EXCHANGE);
  }
}