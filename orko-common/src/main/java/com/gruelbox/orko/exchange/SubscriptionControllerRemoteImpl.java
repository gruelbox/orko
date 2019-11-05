package com.gruelbox.orko.exchange;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.gruelbox.orko.websocket.OrkoWebSocketOutgoingMessage;
import com.gruelbox.orko.websocket.OrkoWebSocketServer;
import com.gruelbox.orko.websocket.OrkoWebsocketStreamingService;

import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.dropwizard.lifecycle.Managed;

/**
 * Remote websocket-based {@link MarketDataSubscriptionManager} interacting with
 * an {@link OrkoWebSocketServer}.
 */
@VisibleForTesting
public class SubscriptionControllerRemoteImpl implements Managed, SubscriptionController {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionControllerRemoteImpl.class);

  private final SubscriptionPublisher publisher;
  private volatile OrkoWebsocketStreamingService streamingService;
  private volatile Set<MarketDataSubscription> subscriptions = Set.of();

  @VisibleForTesting
  @Inject
  public SubscriptionControllerRemoteImpl(SubscriptionPublisher publisher) {
    this.publisher = publisher;
    this.publisher.setController(this);
  }

  @Override
  public void start() throws Exception {
    do {
      try {
        this.streamingService = new OrkoWebsocketStreamingService("ws://localhost:8080/ws");
        this.streamingService.connect().blockingAwait();
        openChannels();
        if (subscriptions != null) {
          this.streamingService.updateSubscriptions(subscriptions);
        }
      } catch (Exception e) {
        if (this.streamingService != null) {
          if (this.streamingService.isSocketOpen())
            this.streamingService.disconnect();
          this.streamingService = null;
        }
        LOGGER.error("Failed to open connection", e);
        Thread.sleep(10000);
      }
    } while (this.streamingService == null && !Thread.currentThread().isInterrupted());
  }

  @Override
  public void stop() throws Exception {
    LOGGER.info("Shutting down...");
    if (this.streamingService != null) {
      LOGGER.info("Closing connection");
      try {
        this.streamingService.disconnect();
        LOGGER.info("Connection closed");
      } catch (Exception e) {
        LOGGER.error("Error closing connection", e);
      }
    }
  }

  private void openChannels() {
    ObjectMapper om = StreamingObjectMapperHelper.getObjectMapper();
    this.streamingService.subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.BALANCE)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, BalanceEvent.class))
        .subscribe(publisher::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.BALANCE));
    this.streamingService.subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.OPEN_ORDERS)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, OpenOrdersEvent.class))
        .subscribe(publisher::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.OPEN_ORDERS));
    this.streamingService.subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.ORDER_STATUS_CHANGE)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, OrderChangeEvent.class))
        .subscribe(publisher::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.ORDER_STATUS_CHANGE));
    this.streamingService.subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.ORDERBOOK)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, OrderBookEvent.class))
        .subscribe(publisher::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.ORDERBOOK));
    this.streamingService.subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.TICKER)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, TickerEvent.class))
        .subscribe(publisher::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.TICKER));
    this.streamingService.subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.TRADE)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, TradeEvent.class))
        .subscribe(publisher::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.TRADE));
    this.streamingService.subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.USER_TRADE)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, UserTradeEvent.class))
        .subscribe(publisher::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.USER_TRADE));
  }

  private void onError(Throwable e, OrkoWebSocketOutgoingMessage.Nature nature) {
    LOGGER.error("Error in {} stream", nature, e);
  }

  @Override
  public void updateSubscriptions(Set<MarketDataSubscription> subscriptions) {
    this.subscriptions = subscriptions;
    if (this.streamingService != null && this.streamingService.isSocketOpen()) {
      this.streamingService.updateSubscriptions(subscriptions);
    }
  }
}