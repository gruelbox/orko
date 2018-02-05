package com.grahamcrockford.oco.orders;

import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.trade.TradeService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.grahamcrockford.oco.api.AdvancedOrder;
import com.grahamcrockford.oco.api.AdvancedOrderInfo;
import com.grahamcrockford.oco.core.AdvancedOrderEnqueuer;
import com.grahamcrockford.oco.core.TelegramService;
import com.grahamcrockford.oco.core.TradeServiceFactory;

public class TestOrderStateNotifierProcessor {

  private static final long JOB_ID = 555L;

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

  @Mock private AdvancedOrderEnqueuer enqueuer;
  @Mock private TelegramService telegramService;

  @Mock private TradeServiceFactory tradeServiceFactory;
  @Mock private TradeService tradeService;
  @Mock private Injector injector;

  private OrderStateNotifierProcessor processor;

  @Before
  public void before() throws IOException {

    MockitoAnnotations.initMocks(this);

    when(tradeServiceFactory.getForExchange(EXCHANGE)).thenReturn(tradeService);

    processor = new OrderStateNotifierProcessor(telegramService, tradeServiceFactory, enqueuer);
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testNotFound1() throws Exception {
    processor.tick(baseJob().build(), null);
    verifySentMessage();
    verifyFinishedJob();
  }

  @Test
  public void testNotFound2() throws Exception {
    when(tradeService.getOrder(ORDER_ID)).thenReturn(Collections.emptyList());
    processor.tick(baseJob().build(), null);
    verifySentMessage();
    verifyFinishedJob();
  }

  @Test
  public void testNotUnique() throws Exception {
    when(tradeService.getOrder(ORDER_ID)).thenReturn(ImmutableList.of(mock(Order.class), mock(Order.class)));
    processor.tick(baseJob().build(), null);
    verifySentMessage();
    verifyFinishedJob();
  }

  @Test
  public void testStatuses() throws Exception {
    for (final Order.OrderStatus status : Order.OrderStatus.values()) {
      Mockito.reset(enqueuer, telegramService);
      when(tradeService.getOrder(ORDER_ID)).thenReturn(ImmutableList.of(new LimitOrder(ASK, AMOUNT, CURRENCY_PAIR, ORDER_ID, new Date(), LIMIT_PRICE, AVERAGE_PRICE, FILLED, status)));
      processor.tick(baseJob().build(), null);
      if (ImmutableSet.of(Order.OrderStatus.PENDING_NEW, Order.OrderStatus.NEW, Order.OrderStatus.PARTIALLY_FILLED).contains(status)) {
        verifyNoChanges(baseJob().build());
      } else {
        verifySentMessage();
        verifyFinishedJob();
      }
    }
  }

  /* ---------------------------------- Utility methods  ---------------------------------------------------- */

  private void verifyNoChanges(AdvancedOrder order) {
    verifyZeroInteractions(telegramService);
    verify(enqueuer).enqueueAfterConfiguredDelay(order);
  }

  private void verifySentMessage() {
    verify(telegramService).sendMessage(Mockito.anyString());
  }

  private void verifyFinishedJob() {
    verifyZeroInteractions(enqueuer);
  }

  private OrderStateNotifier.Builder baseJob() {
    return OrderStateNotifier.builder()
      .id(JOB_ID)
      .description(DESCRIPTION)
      .orderId(ORDER_ID)
      .basic(AdvancedOrderInfo.builder()
        .base(BASE)
        .counter(COUNTER)
        .exchange(EXCHANGE)
        .build()
       );
  }
}