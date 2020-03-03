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
package com.gruelbox.orko.websocket;

import static com.gruelbox.orko.exchange.MarketDataType.BALANCE;
import static com.gruelbox.orko.exchange.MarketDataType.OPEN_ORDERS;
import static com.gruelbox.orko.exchange.MarketDataType.ORDER;
import static com.gruelbox.orko.exchange.MarketDataType.ORDERBOOK;
import static com.gruelbox.orko.exchange.MarketDataType.TICKER;
import static com.gruelbox.orko.exchange.MarketDataType.TRADES;
import static com.gruelbox.orko.exchange.MarketDataType.USER_TRADE;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.gruelbox.orko.exchange.ExchangeEventRegistry;
import com.gruelbox.orko.exchange.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.exchange.MarketDataSubscription;
import com.gruelbox.orko.exchange.MarketDataType;
import com.gruelbox.orko.exchange.SerializableTrade;
import com.gruelbox.orko.exchange.SerializableTradeEvent;
import com.gruelbox.orko.exchange.TradeEvent;
import com.gruelbox.orko.exchange.UserTradeEvent;
import com.gruelbox.orko.jobrun.spi.StatusUpdate;
import com.gruelbox.orko.notification.Notification;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.SafelyClose;
import com.gruelbox.orko.util.SafelyDispose;
import com.gruelbox.orko.websocket.OrkoWebSocketOutgoingMessage.Nature;
import io.reactivex.disposables.Disposable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Metered
@Timed
@ExceptionMetered
@ServerEndpoint(WebSocketModule.ENTRY_POINT)
public final class OrkoWebSocketServer {

  private static final int READY_TIMEOUT = 5000;
  private static final Logger LOGGER = LoggerFactory.getLogger(OrkoWebSocketServer.class);

  private ExchangeEventRegistry exchangeEventRegistry;
  private ObjectMapper objectMapper;
  private EventBus eventBus;

  private final AtomicLong lastReadyTime = new AtomicLong();
  private final AtomicReference<ImmutableSet<MarketDataSubscription>> marketDataSubscriptions =
      new AtomicReference<>(ImmutableSet.of());

  private Session session;
  private Disposable disposable;
  private ExchangeEventSubscription subscription;

  @OnOpen
  public synchronized void myOnOpen(final javax.websocket.Session session) {
    LOGGER.info("Opening socket");
    markReady();
    Injector injector = (Injector) session.getUserProperties().get(Injector.class.getName());
    injector.injectMembers(this);
    this.session = session;
    eventBus.register(this);
  }

  @Inject
  void inject(
      ExchangeEventRegistry exchangeEventRegistry, ObjectMapper objectMapper, EventBus eventBus) {
    this.exchangeEventRegistry = exchangeEventRegistry;
    this.objectMapper = objectMapper;
    this.eventBus = eventBus;
  }

  @OnMessage
  public void myOnMsg(final javax.websocket.Session session, String message) {
    OrkoWebSocketIncomingMessage request = null;
    try {

      LOGGER.debug("Received websocket message: {}", message);
      request = decodeRequest(message);

      switch (request.command()) {
        case READY:
          markReady();
          break;
        case CHANGE_TICKERS:
          mutateSubscriptions(TICKER, request.tickers());
          break;
        case CHANGE_OPEN_ORDERS:
          mutateSubscriptions(OPEN_ORDERS, request.tickers());
          break;
        case CHANGE_ORDER_BOOK:
          mutateSubscriptions(ORDERBOOK, request.tickers());
          break;
        case CHANGE_TRADES:
          mutateSubscriptions(TRADES, request.tickers());
          break;
        case CHANGE_USER_TRADES:
          mutateSubscriptions(USER_TRADE, request.tickers());
          break;
        case CHANGE_BALANCE:
          mutateSubscriptions(BALANCE, request.tickers());
          break;
        case CHANGE_ORDER_STATUS_CHANGE:
          mutateSubscriptions(ORDER, request.tickers());
          break;
        case UPDATE_SUBSCRIPTIONS:
          updateSubscriptions();
          break;
        default:
          // Jackson should stop this happening in the try block above, but just for completeness
          throw new IllegalArgumentException("Invalid command: " + request.command());
      }

    } catch (Exception e) {
      LOGGER.error("Error processing message: " + message, e);
      send("Error processing message", Nature.ERROR);
    }
  }

  private void markReady() {
    LOGGER.debug("Client is ready");
    lastReadyTime.set(System.currentTimeMillis());
  }

  private boolean isReady() {
    boolean result = (System.currentTimeMillis() - lastReadyTime.get()) < READY_TIMEOUT;
    if (!result) LOGGER.debug("Suppressing outgoing message as client is not ready");
    return result;
  }

  private void mutateSubscriptions(MarketDataType marketDataType, Iterable<TickerSpec> tickers) {
    if (tickers == null) tickers = Set.of();
    marketDataSubscriptions.set(
        ImmutableSet.<MarketDataSubscription>builder()
            .addAll(
                FluentIterable.from(marketDataSubscriptions.get())
                    .filter(sub -> !sub.type().equals(marketDataType)))
            .addAll(
                FluentIterable.from(tickers)
                    .transform(spec -> MarketDataSubscription.create(spec, marketDataType)))
            .build());
  }

  @OnClose
  public synchronized void myOnClose(final javax.websocket.Session session, CloseReason cr) {
    LOGGER.info("Closing socket ({})", cr);
    SafelyDispose.of(disposable);
    disposable = null;
    marketDataSubscriptions.set(ImmutableSet.of());
    SafelyClose.the(subscription);
    subscription = null;
  }

  @OnError
  public void onError(Throwable error) {
    LOGGER.error("Socket error", error);
  }

  private OrkoWebSocketIncomingMessage decodeRequest(String message) {
    OrkoWebSocketIncomingMessage request;
    try {
      request = objectMapper.readValue(message, OrkoWebSocketIncomingMessage.class);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid request", e);
    }
    return request;
  }

  private synchronized void updateSubscriptions() {
    Set<MarketDataSubscription> target = marketDataSubscriptions.get();
    LOGGER.debug("Updating subscriptions to {}", target);
    SafelyDispose.of(disposable);
    if (subscription == null) {
      subscription = exchangeEventRegistry.subscribe(target);
    } else {
      subscription = subscription.replace(target);
    }
    disposable =
        new Disposable() {

          // Apply a 1-second throttle on a PER TICKER basis
          private final List<Disposable> tickers =
              FluentIterable.from(subscription.getTickersSplit())
                  .transform(
                      f ->
                          f.filter(e -> isReady())
                              .throttleLast(1, TimeUnit.SECONDS)
                              .subscribe(e -> send(e, Nature.TICKER)))
                  .toList();

          // Order book should be throttled globally
          private final Disposable orderBook =
              subscription
                  .getOrderBooks()
                  .filter(o -> isReady())
                  .throttleLast(1, TimeUnit.SECONDS)
                  .subscribe(e -> send(e, Nature.ORDERBOOK));

          // Trades, balances and order status changes are unthrottled - the assumption is that you
          // need the lot
          private final Disposable trades =
              subscription
                  .getTrades()
                  .filter(o -> isReady())
                  .map(OrkoWebSocketServer.this::serialiseTradeEvent)
                  .subscribe(e -> send(e, Nature.TRADE));
          private final Disposable orders =
              subscription
                  .getOrderChanges()
                  .filter(o -> isReady())
                  .subscribe(e -> send(e, Nature.ORDER_STATUS_CHANGE));
          private final Disposable userTrades =
              subscription
                  .getUserTrades()
                  .filter(o -> isReady())
                  .map(OrkoWebSocketServer.this::serialiseUserTradeEvent)
                  .subscribe(e -> send(e, Nature.USER_TRADE));
          private final Disposable balance =
              subscription
                  .getBalances()
                  .filter(o -> isReady())
                  .subscribe(e -> send(e, Nature.BALANCE));

          // And the rest are fetched by poll, so there's no benefit to throttling
          private final Disposable openOrders =
              subscription
                  .getOrderSnapshots()
                  .filter(o -> isReady())
                  .subscribe(e -> send(e, Nature.OPEN_ORDERS));

          @Override
          public boolean isDisposed() {
            return openOrders.isDisposed()
                && orderBook.isDisposed()
                && tickers.stream().allMatch(Disposable::isDisposed)
                && trades.isDisposed()
                && orders.isDisposed()
                && userTrades.isDisposed()
                && balance.isDisposed();
          }

          @Override
          public void dispose() {
            SafelyDispose.of(openOrders, orderBook, trades, orders, userTrades, balance);
            SafelyDispose.of(tickers);
          }
        };
  }

  /** Workaround for lack of serializability of the XChange object */
  private Object serialiseUserTradeEvent(UserTradeEvent e) {
    return SerializableTradeEvent.create(
        e.spec(), SerializableTrade.create(e.spec().exchange(), e.trade()));
  }

  /** Workaround for lack of serializability of the XChange object */
  private Object serialiseTradeEvent(TradeEvent e) {
    return SerializableTradeEvent.create(
        e.spec(), SerializableTrade.create(e.spec().exchange(), e.trade()));
  }

  @Subscribe
  void onNotification(Notification notification) {
    send(notification, Nature.NOTIFICATION);
  }

  @Subscribe
  void onStatusUpdate(StatusUpdate statusUpdate) {
    send(statusUpdate, Nature.STATUS_UPDATE);
  }

  /**
   * Synchronized so we send backpressure down the channels and feed data through as fast as it can
   * be used.
   */
  private synchronized void send(Object object, Nature nature) {
    LOGGER.debug("{}: {}", nature, object);
    try {
      if (session.isOpen()) session.getBasicRemote().sendText(message(nature, object));
    } catch (Exception e) {
      LOGGER.warn("Failed to send {} to socket ({})", nature, e.getMessage());
    }
  }

  private String message(Nature nature, Object data) {
    try {
      return objectMapper.writeValueAsString(OrkoWebSocketOutgoingMessage.create(nature, data));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
