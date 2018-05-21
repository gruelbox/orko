package com.grahamcrockford.oco.websocket;

import static com.grahamcrockford.oco.marketdata.MarketDataType.OPEN_ORDERS;
import static com.grahamcrockford.oco.marketdata.MarketDataType.ORDERBOOK;
import static com.grahamcrockford.oco.marketdata.MarketDataType.TICKER;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.security.RolesAllowed;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.grahamcrockford.oco.auth.Roles;
import com.grahamcrockford.oco.marketdata.ExchangeEventRegistry;
import com.grahamcrockford.oco.marketdata.MarketDataSubscription;
import com.grahamcrockford.oco.marketdata.MarketDataType;
import com.grahamcrockford.oco.notification.NotificationEvent;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.websocket.OcoWebSocketOutgoingMessage.Nature;

import io.reactivex.disposables.Disposable;

@Metered
@Timed
@ExceptionMetered
@ServerEndpoint("/ws")
@RolesAllowed(Roles.TRADER)
public final class OcoWebSocketServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(OcoWebSocketServer.class);

  private final String eventRegistryClientId = OcoWebSocketServer.class.getSimpleName() + "/" + UUID.randomUUID().toString();

  @Inject private ExchangeEventRegistry exchangeEventRegistry;
  @Inject private ObjectMapper objectMapper;
  @Inject private AsyncEventBus eventBus;

  private volatile Session session;
  private volatile Disposable subscription;

  private final AtomicReference<ImmutableSet<MarketDataSubscription>> marketDataSubscriptions = new AtomicReference<>(ImmutableSet.of());

  @OnOpen
  public void myOnOpen(final javax.websocket.Session session) throws IOException, InterruptedException {
    LOGGER.info("Opening socket");
    injectMembers(session);
    this.session = session;
    eventBus.register(this);
  }

  @OnMessage
  public void myOnMsg(final javax.websocket.Session session, String message) {
    OcoWebSocketIncomingMessage request = null;
    try {

      LOGGER.debug("Received websocket message: {}", message);
      request = decodeRequest(message);

      switch (request.command()) {
        case CHANGE_TICKERS:
          mutateSubscriptions(TICKER, request.tickers());
          break;
        case CHANGE_OPEN_ORDERS:
          mutateSubscriptions(OPEN_ORDERS, request.tickers());
          break;
        case CHANGE_ORDER_BOOK:
          mutateSubscriptions(ORDERBOOK, request.tickers());
          break;
        case UPDATE_SUBSCRIPTIONS:
          updateSubscriptions(session);
          break;
        default:
          // Jackson should stop this happening in the try block above, but just for completeness
          throw new IllegalArgumentException("Invalid command: " + request.command());
      }

    } catch (Exception e) {
      LOGGER.error("Error processing message: " + message, e);
      session.getAsyncRemote().sendText(message(Nature.ERROR, "Error processing message"));
      return;
    }
  }


  private void mutateSubscriptions(MarketDataType marketDataType, Iterable<TickerSpec> tickers) {
    marketDataSubscriptions.set(ImmutableSet.<MarketDataSubscription>builder()
      .addAll(FluentIterable.from(marketDataSubscriptions.get()).filter(sub -> !sub.type().equals(marketDataType)))
      .addAll(FluentIterable.from(tickers).transform(spec -> MarketDataSubscription.create(spec, marketDataType)))
      .build()
    );
  }

  @OnClose
  public synchronized void myOnClose(final javax.websocket.Session session, CloseReason cr) {
    LOGGER.info("Closing socket ({})", cr.toString());
    if (subscription != null)
      subscription.dispose();
    subscription = null;
    marketDataSubscriptions.set(ImmutableSet.of());
    try {
      exchangeEventRegistry.clearSubscriptions(eventRegistryClientId);
    } catch (Throwable t) {
      LOGGER.error("Error unregistering socket from ticker", t);
    }
  }

  @OnError
  public void onError(Throwable error) {
    LOGGER.error("Socket error", error);
  }

  private void injectMembers(final javax.websocket.Session session) {
    Injector injector = (Injector) session.getUserProperties().get(Injector.class.getName());
    injector.injectMembers(this);
  }

  private OcoWebSocketIncomingMessage decodeRequest(String message) {
    OcoWebSocketIncomingMessage request;
    try {
      request = objectMapper.readValue(message, OcoWebSocketIncomingMessage.class);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid request", e);
    }
    return request;
  }

  private synchronized void updateSubscriptions(Session session) {
    if (subscription != null)
      subscription.dispose();
    exchangeEventRegistry.changeSubscriptions(eventRegistryClientId, marketDataSubscriptions.get());
    subscription = new Disposable() {

      private final Disposable openOrders = exchangeEventRegistry.getOpenOrders(eventRegistryClientId).subscribe(e -> send(e, Nature.OPEN_ORDERS));
      private final Disposable orderBook = exchangeEventRegistry.getOrderBooks(eventRegistryClientId).subscribe(e -> send(e, Nature.ORDERBOOK));
      private final Disposable tickers = exchangeEventRegistry.getTickers(eventRegistryClientId).subscribe(e -> send(e, Nature.TICKER));

      @Override
      public boolean isDisposed() {
        return openOrders.isDisposed() && orderBook.isDisposed() && tickers.isDisposed();
      }

      @Override
      public void dispose() {
        try {
          openOrders.dispose();
        } catch (Throwable t) {
          LOGGER.error("Error disposing of openOrders subscription", t);
        }
        try {
          orderBook.dispose();
        } catch (Throwable t) {
          LOGGER.error("Error disposing of orderBook subscription", t);
        }
        try {
          tickers.dispose();
        } catch (Throwable t) {
          LOGGER.error("Error disposing of tickers subscription", t);
        }
      }
    };
  }

  @Subscribe
  void onNotification(NotificationEvent notificationEvent) {
    send(notificationEvent, Nature.NOTIFICATION);
  }

  /**
   * Synchronized so we send backpressure down the channels and feed data through
   * as fast as it can be used.
   */
  private synchronized void send(Object object, Nature nature) {
    LOGGER.debug("{}: {}", nature, object);
    try {
      if (session.isOpen())
        session.getBasicRemote().sendText(message(nature, object));
    } catch (IOException e) {
      LOGGER.info("Failed to send " + object + " to socket", e);
    }
  }

  private String message(Nature nature, Object data) {
    try {
      return objectMapper.writeValueAsString(OcoWebSocketOutgoingMessage.create(nature, data));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}