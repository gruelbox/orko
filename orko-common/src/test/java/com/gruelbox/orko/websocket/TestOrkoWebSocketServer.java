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
package com.gruelbox.orko.websocket;

import static com.gruelbox.orko.marketdata.MarketDataType.BALANCE;
import static com.gruelbox.orko.marketdata.MarketDataType.OPEN_ORDERS;
import static com.gruelbox.orko.marketdata.MarketDataType.ORDER;
import static com.gruelbox.orko.marketdata.MarketDataType.ORDERBOOK;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static com.gruelbox.orko.marketdata.MarketDataType.TRADES;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.knowm.xchange.currency.Currency.BTC;
import static org.knowm.xchange.currency.Currency.USD;
import static org.knowm.xchange.currency.CurrencyPair.ADA_BNB;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.CloseReason;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrade;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.inject.Injector;
import com.gruelbox.orko.marketdata.Balance;
import com.gruelbox.orko.marketdata.BalanceEvent;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry;
import com.gruelbox.orko.marketdata.MarketDataSubscription;
import com.gruelbox.orko.marketdata.MarketDataType;
import com.gruelbox.orko.marketdata.OpenOrdersEvent;
import com.gruelbox.orko.marketdata.OrderBookEvent;
import com.gruelbox.orko.marketdata.OrderChangeEvent;
import com.gruelbox.orko.marketdata.TickerEvent;
import com.gruelbox.orko.marketdata.TradeEvent;
import com.gruelbox.orko.marketdata.UserTradeEvent;
import com.gruelbox.orko.spi.TickerSpec;

import io.reactivex.Flowable;

public class TestOrkoWebSocketServer {

  private static final TickerSpec SPEC = TickerSpec.fromKey("binance/USD/BTC");
  @Mock private Injector injector;
  @Mock private Session session;
  @Mock private ExchangeEventRegistry exchangeEventRegistry;
  @Mock private ExchangeEventRegistry.ExchangeEventSubscription subscription;
  @Mock private RemoteEndpoint.Basic remote;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final EventBus eventBus = new EventBus();

  private OrkoWebSocketServer server;

  @Before
  public void setup() {

    server = new OrkoWebSocketServer();
    MockitoAnnotations.initMocks(this);
    when(session.getUserProperties()).thenReturn(ImmutableMap.of(Injector.class.getName(), injector));
    doAnswer(inv -> {
      server.inject(exchangeEventRegistry, objectMapper, eventBus);
      return null;
    }).when(injector).injectMembers(server);
    when(session.isOpen()).thenReturn(true);
    when(session.getBasicRemote()).thenReturn(remote);

    when(subscription.getBalances()).thenReturn(Flowable.empty());
    when(subscription.getOrderBooks()).thenReturn(Flowable.empty());
    when(subscription.getOrderChanges()).thenReturn(Flowable.empty());
    when(subscription.getOrderSnapshots()).thenReturn(Flowable.empty());
    when(subscription.getTickersSplit()).thenReturn(ImmutableList.of(Flowable.empty()));
    when(subscription.getTrades()).thenReturn(Flowable.empty());
    when(subscription.getUserTrades()).thenReturn(Flowable.empty());

    server.myOnOpen(session);
  }

  @After
  public void tearDown() {
    server.myOnClose(session, new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Done"));
  }

  @Test
  public void testOpenClose() {
    // Just the before/after
  }

  @Test
  public void testOnError() {
    server.onError(new IllegalArgumentException("Boo"));
  }

  @Test
  public void testTickers() throws IOException, InterruptedException {
    TickerEvent event1 = TickerEvent.create(SPEC, new Ticker.Builder().last(ZERO).build());
    TickerEvent event2 = TickerEvent.create(SPEC, new Ticker.Builder().last(TEN).build());
    Flowable<TickerEvent> events = Flowable.just(event1, event2, event1)
        .concatMap(e -> Flowable.just(e).delay(1500, TimeUnit.MILLISECONDS))
        .doOnNext(e -> System.out.println("Emitting " + e));
    when(subscription.getTickersSplit()).thenReturn(ImmutableList.of(events));
    List<String> messagesSent = messaging("CHANGE_TICKERS", TICKER, true);
    assertThat(messagesSent, contains(
        startsWith("{\"nature\":\"TICKER\",\"data\":{"),
        startsWith("{\"nature\":\"TICKER\",\"data\":{")
    ));
  }

  @Test
  public void testBalances() throws IOException, InterruptedException {
    BalanceEvent event1 = BalanceEvent.create("binance", "USD", Balance.create(new org.knowm.xchange.dto.account.Balance.Builder().currency(USD).total(ZERO).build()));
    BalanceEvent event2 = BalanceEvent.create("binance", "BTC", Balance.create(new org.knowm.xchange.dto.account.Balance.Builder().currency(BTC).total(ZERO).build()));
    Flowable<BalanceEvent> events = Flowable.just(event1, event2);
    when(subscription.getBalances()).thenReturn(events);
    List<String> messagesSent = messaging("CHANGE_BALANCE", BALANCE, false);
    assertThat(messagesSent, contains(
        startsWith("{\"nature\":\"BALANCE\",\"data\":{"),
        startsWith("{\"nature\":\"BALANCE\",\"data\":{")
    ));
  }

  @Test
  public void testOrderChanges() throws IOException, InterruptedException {
    OrderChangeEvent event1 = OrderChangeEvent.create(SPEC, new LimitOrder.Builder(ASK, ADA_BNB).build(), new Date());
    OrderChangeEvent event2 = OrderChangeEvent.create(SPEC, new LimitOrder.Builder(ASK, ADA_BNB).build(), new Date());
    Flowable<OrderChangeEvent> events = Flowable.just(event1, event2);
    when(subscription.getOrderChanges()).thenReturn(events);
    List<String> messagesSent = messaging("CHANGE_ORDER_STATUS_CHANGE", ORDER, false);
    assertThat(messagesSent, contains(
        startsWith("{\"nature\":\"ORDER_STATUS_CHANGE\",\"data\":{"),
        startsWith("{\"nature\":\"ORDER_STATUS_CHANGE\",\"data\":{")
    ));
  }

  @Test
  public void testOrderSnapshots() throws IOException, InterruptedException {
    OpenOrdersEvent event1 = OpenOrdersEvent.create(SPEC, new OpenOrders(ImmutableList.of()), new Date());
    OpenOrdersEvent event2 = OpenOrdersEvent.create(SPEC, new OpenOrders(ImmutableList.of()), new Date());
    Flowable<OpenOrdersEvent> events = Flowable.just(event1, event2);
    when(subscription.getOrderSnapshots()).thenReturn(events);
    List<String> messagesSent = messaging("CHANGE_OPEN_ORDERS", OPEN_ORDERS, false);
    assertThat(messagesSent, contains(
        startsWith("{\"nature\":\"OPEN_ORDERS\",\"data\":{"),
        startsWith("{\"nature\":\"OPEN_ORDERS\",\"data\":{")
    ));
  }

  @Test
  public void testTrades() throws IOException, InterruptedException {
    TradeEvent event1 = TradeEvent.create(SPEC, new Trade.Builder().currencyPair(ADA_BNB).timestamp(new Date()).type(ASK).originalAmount(ZERO).price(ZERO).build());
    TradeEvent event2 = TradeEvent.create(SPEC, new Trade.Builder().currencyPair(ADA_BNB).timestamp(new Date()).type(ASK).originalAmount(ZERO).price(ZERO).build());
    Flowable<TradeEvent> events = Flowable.just(event1, event2);
    when(subscription.getTrades()).thenReturn(events);
    List<String> messagesSent = messaging("CHANGE_TRADES", TRADES, false);
    assertThat(messagesSent, contains(
        startsWith("{\"nature\":\"TRADE\",\"data\":{"),
        startsWith("{\"nature\":\"TRADE\",\"data\":{")
    ));
  }

  @Test
  public void testUserTrades() throws IOException, InterruptedException {
    UserTradeEvent event1 = UserTradeEvent.create(SPEC, new UserTrade.Builder().currencyPair(ADA_BNB).feeCurrency(BTC).timestamp(new Date()).type(ASK).originalAmount(ZERO).price(ZERO).build());
    UserTradeEvent event2 = UserTradeEvent.create(SPEC, new UserTrade.Builder().currencyPair(ADA_BNB).feeCurrency(BTC).timestamp(new Date()).type(ASK).originalAmount(ZERO).price(ZERO).build());
    Flowable<UserTradeEvent> events = Flowable.just(event1, event2);
    when(subscription.getUserTrades()).thenReturn(events);
    List<String> messagesSent = messaging("CHANGE_USER_TRADES", MarketDataType.USER_TRADE, false);
    assertThat(messagesSent, contains(
        startsWith("{\"nature\":\"USER_TRADE\",\"data\":{"),
        startsWith("{\"nature\":\"USER_TRADE\",\"data\":{")
    ));
  }

  @Test
  public void testOrderBooks() throws IOException, InterruptedException {
    TickerSpec tickerSpec = SPEC;
    OrderBookEvent event1 = OrderBookEvent.create(tickerSpec, new OrderBook(new Date(), ImmutableList.of(), ImmutableList.of()));
    OrderBookEvent event2 = OrderBookEvent.create(tickerSpec, new OrderBook(new Date(), ImmutableList.of(), ImmutableList.of()));
    Flowable<OrderBookEvent> events = Flowable.just(event1, event2, event1)
        .concatMap(e -> Flowable.just(e).delay(2200, TimeUnit.MILLISECONDS))
        .doOnNext(e -> System.out.println("Emitting " + e));
    when(subscription.getOrderBooks()).thenReturn(events);
    List<String> messagesSent = messaging("CHANGE_ORDER_BOOK", ORDERBOOK, true);
    assertThat(messagesSent, contains(
        startsWith("{\"nature\":\"ORDERBOOK\",\"data\":{"),
        startsWith("{\"nature\":\"ORDERBOOK\",\"data\":{")
    ));
  }

  private List<String> messaging(String command, MarketDataType dataType, boolean longRunning) throws IOException, InterruptedException {
    when(exchangeEventRegistry.subscribe(ImmutableSet.of(MarketDataSubscription.create(SPEC, dataType))))
        .thenReturn(subscription);

    List<String> messagesSent = new CopyOnWriteArrayList<>();
    CountDownLatch callCounter = new CountDownLatch(2);
    doAnswer(inv -> {
      messagesSent.add(inv.getArgument(0));
      callCounter.countDown();
      return null;
    }).when(remote).sendText(Mockito.anyString());

    Thread keepalive = longRunning ? new Thread(() -> {
      while (true) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e1) {
          break;
        }
        server.myOnMsg(session, "{ \"command\": \"READY\" }");
      }
    }) : null;

    String tickerString = "[ { \"exchange\": \"binance\", \"counter\": \"USD\", \"base\": \"BTC\" } ]";
    if (longRunning)
      keepalive.start();
    try {
      server.myOnMsg(session, "{ \"command\": \"" + command + "\", \"tickers\": " + tickerString + " }");
      server.myOnMsg(session, "{ \"command\": \"UPDATE_SUBSCRIPTIONS\" }");
      assertTrue(callCounter.await(20, TimeUnit.SECONDS));
    } finally {
      if (longRunning)
        keepalive.interrupt();
    }

    return messagesSent;
  }
}
