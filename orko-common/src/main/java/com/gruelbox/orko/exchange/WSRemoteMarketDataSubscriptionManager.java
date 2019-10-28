package com.gruelbox.orko.exchange;

import static com.gruelbox.orko.exchange.MarketDataType.BALANCE;
import static com.gruelbox.orko.exchange.MarketDataType.OPEN_ORDERS;
import static com.gruelbox.orko.exchange.MarketDataType.ORDER;
import static com.gruelbox.orko.exchange.MarketDataType.TRADES;
import static com.gruelbox.orko.exchange.MarketDataType.USER_TRADE;
import static com.gruelbox.orko.websocket.OrkoWebSocketIncomingMessage.Command.CHANGE_BALANCE;
import static com.gruelbox.orko.websocket.OrkoWebSocketIncomingMessage.Command.CHANGE_OPEN_ORDERS;
import static com.gruelbox.orko.websocket.OrkoWebSocketIncomingMessage.Command.CHANGE_ORDER_BOOK;
import static com.gruelbox.orko.websocket.OrkoWebSocketIncomingMessage.Command.CHANGE_ORDER_STATUS_CHANGE;
import static com.gruelbox.orko.websocket.OrkoWebSocketIncomingMessage.Command.CHANGE_TICKERS;
import static com.gruelbox.orko.websocket.OrkoWebSocketIncomingMessage.Command.CHANGE_TRADES;
import static com.gruelbox.orko.websocket.OrkoWebSocketIncomingMessage.Command.CHANGE_USER_TRADES;
import static com.gruelbox.orko.websocket.OrkoWebSocketIncomingMessage.Command.UPDATE_SUBSCRIPTIONS;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Set;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.knowm.xchange.utils.ObjectMapperHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.websocket.OrkoWebSocketIncomingMessage;
import com.gruelbox.orko.websocket.OrkoWebSocketOutgoingMessage;
import com.gruelbox.orko.websocket.WebSocketClient;
import com.gruelbox.orko.wiring.BackgroundProcessingConfiguration;

import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

class WSRemoteMarketDataSubscriptionManager extends AbstractMarketDataSubscriptionManager {

  private volatile Set<MarketDataSubscription> subscriptions = Set.of();
  private volatile Session session;

  @Inject
  WSRemoteMarketDataSubscriptionManager(BackgroundProcessingConfiguration configuration) {
    super(configuration);
  }

  @Override
  protected void doRun() throws InterruptedException {
    do {
      try {
        Thread.sleep(2000);
        subscribe();
        break;
      } catch (Exception e) {
        logger.error("Failed to open subscriptions", e);
      }
    } while (!Thread.currentThread().isInterrupted() && !isTerminated());

    while (!Thread.currentThread().isInterrupted() && !isTerminated()) {
      if (!session.isOpen()) {
        Thread.sleep(10000);
        subscribe();
      }
      sendMessage(OrkoWebSocketIncomingMessage.Command.READY, Set.of());
      Thread.sleep(2000);
    }

    if (session != null) {
      logger.info("Closing connection to {}", session.getRequestURI());
      try {
        session.close();
        logger.debug("Connection closed");
      } catch (IOException e) {
        logger.error("Error closing connection", e);
      }
    }
  }

  private void subscribe() {
    ObjectMapper om = StreamingObjectMapperHelper.getObjectMapper();

    Flowable<OrkoWebSocketOutgoingMessage> data = Flowable.<String>create(emitter -> {
      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      this.session = container.connectToServer(new WebSocketClient(emitter), URI.create("ws://localhost:8080/ws"));
    }, BackpressureStrategy.DROP)
        .map(txt -> ObjectMapperHelper.readValue(txt, OrkoWebSocketOutgoingMessage.class))
        .doOnError(e -> logger.error("Error from websocket", e))
        .share();

    data.filter(msg -> msg.nature().equals(OrkoWebSocketOutgoingMessage.Nature.BALANCE))
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, BalanceEvent.class))
        .subscribe(balanceOut::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.BALANCE));
    data.filter(msg -> msg.nature().equals(OrkoWebSocketOutgoingMessage.Nature.OPEN_ORDERS))
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, OpenOrdersEvent.class))
        .subscribe(openOrdersOut::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.OPEN_ORDERS));
    data.filter(msg -> msg.nature().equals(OrkoWebSocketOutgoingMessage.Nature.ORDER_STATUS_CHANGE))
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, OrderChangeEvent.class))
        .subscribe(orderStatusChangeOut::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.ORDER_STATUS_CHANGE));
    data.filter(msg -> msg.nature().equals(OrkoWebSocketOutgoingMessage.Nature.ORDERBOOK))
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, OrderBookEvent.class))
        .subscribe(orderbookOut::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.ORDERBOOK));
    data.filter(msg -> msg.nature().equals(OrkoWebSocketOutgoingMessage.Nature.TICKER))
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, TickerEvent.class))
        .subscribe(tickersOut::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.TICKER));
    data.filter(msg -> msg.nature().equals(OrkoWebSocketOutgoingMessage.Nature.TRADE))
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, TradeEvent.class))
        .subscribe(tradesOut::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.TRADE));
    data.filter(msg -> msg.nature().equals(OrkoWebSocketOutgoingMessage.Nature.USER_TRADE))
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, UserTradeEvent.class))
        .subscribe(userTradesOut::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.USER_TRADE));

    updateSubscriptions(subscriptions);
  }

  private void onError(Throwable e, OrkoWebSocketOutgoingMessage.Nature nature) {
    logger.error("Error in {} stream", nature, e);
  }

  @Override
  public void updateSubscriptions(Set<MarketDataSubscription> subscriptions) {
    this.subscriptions = Set.copyOf(subscriptions);
    sendSubscriptions();
  }

  private void sendSubscriptions() {
    logger.info("Sending {} resubscriptions", subscriptions.size());
    sendSubscription(subscriptions, MarketDataType.TICKER, CHANGE_TICKERS);
    sendSubscription(subscriptions, BALANCE, CHANGE_BALANCE);
    sendSubscription(subscriptions, OPEN_ORDERS, CHANGE_OPEN_ORDERS);
    sendSubscription(subscriptions, ORDER, CHANGE_ORDER_STATUS_CHANGE);
    sendSubscription(subscriptions, MarketDataType.ORDERBOOK, CHANGE_ORDER_BOOK);
    sendSubscription(subscriptions, TRADES, CHANGE_TRADES);
    sendSubscription(subscriptions, USER_TRADE, CHANGE_USER_TRADES);
    sendMessage(UPDATE_SUBSCRIPTIONS, null);
    logger.debug("{} resubscriptions sent", subscriptions.size());
  }

  private void sendSubscription(Set<MarketDataSubscription> subscriptions, MarketDataType marketDataType, OrkoWebSocketIncomingMessage.Command command) {
    sendMessage(command,
        subscriptions.stream()
            .filter(it -> it.type() == marketDataType)
            .map(MarketDataSubscription::spec)
            .collect(toSet()));
  }

  private void sendMessage(OrkoWebSocketIncomingMessage.Command command, Collection<TickerSpec> tickers) {
    try {
      session.getBasicRemote().sendText(
          ObjectMapperHelper.toCompactJSON(
              OrkoWebSocketIncomingMessage.create(
                  command,
                  tickers)));
    } catch (IOException e) {
      logger.error("Error sending {} message", command, e);
    }
  }
}