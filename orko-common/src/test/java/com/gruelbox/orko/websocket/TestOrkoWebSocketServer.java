package com.gruelbox.orko.websocket;

import static com.gruelbox.orko.marketdata.MarketDataType.BALANCE;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.knowm.xchange.currency.Currency.BTC;
import static org.knowm.xchange.currency.Currency.USD;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import org.knowm.xchange.dto.marketdata.Ticker;
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
import com.gruelbox.orko.marketdata.TickerEvent;
import com.gruelbox.orko.spi.TickerSpec;

import io.reactivex.Flowable;

public class TestOrkoWebSocketServer {

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
    when(subscription.getTickers()).thenReturn(Flowable.empty());
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
    TickerSpec tickerSpec = TickerSpec.fromKey("binance/USD/BTC");
    TickerEvent event1 = TickerEvent.create(tickerSpec, new Ticker.Builder().last(ZERO).build());
    TickerEvent event2 = TickerEvent.create(tickerSpec, new Ticker.Builder().last(TEN).build());
    Flowable<TickerEvent> events = Flowable.just(event1, event2, event1)
        .concatMap(e -> Flowable.just(e).delay(1500, TimeUnit.MILLISECONDS))
        .doOnNext(e -> System.out.println("Emitting " + e));
    when(subscription.getTickersSplit()).thenReturn(ImmutableList.of(events));
    when(exchangeEventRegistry.subscribe(ImmutableSet.of(MarketDataSubscription.create(tickerSpec, TICKER))))
        .thenReturn(subscription);

    List<String> messagesSent = messaging("CHANGE_TICKERS", true);
    assertThat(messagesSent, contains(
        startsWith("{\"nature\":\"TICKER\",\"data\":{"),
        startsWith("{\"nature\":\"TICKER\",\"data\":{")
    ));
  }

  @Test
  public void testBalances() throws IOException, InterruptedException {
    TickerSpec tickerSpec = TickerSpec.fromKey("binance/USD/BTC");
    BalanceEvent event1 = BalanceEvent.create("binance", "USD", Balance.create(new org.knowm.xchange.dto.account.Balance.Builder().currency(USD).total(ZERO).build()));
    BalanceEvent event2 = BalanceEvent.create("binance", "BTC", Balance.create(new org.knowm.xchange.dto.account.Balance.Builder().currency(BTC).total(ZERO).build()));
    Flowable<BalanceEvent> events = Flowable.just(event1, event2);
    when(subscription.getBalances()).thenReturn(events);
    when(exchangeEventRegistry.subscribe(ImmutableSet.of(MarketDataSubscription.create(tickerSpec, BALANCE))))
        .thenReturn(subscription);

    List<String> messagesSent = messaging("CHANGE_BALANCE", false);
    assertThat(messagesSent, contains(
        startsWith("{\"nature\":\"BALANCE\",\"data\":{"),
        startsWith("{\"nature\":\"BALANCE\",\"data\":{")
    ));
  }

  private List<String> messaging(String command, boolean longRunning) throws IOException, InterruptedException {
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
      assertTrue(callCounter.await(10, TimeUnit.SECONDS));
    } finally {
      if (longRunning)
        keepalive.interrupt();
    }

    return messagesSent;
  }
}
