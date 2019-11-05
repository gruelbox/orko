package com.gruelbox.orko.exchange;

import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.gruelbox.orko.websocket.OrkoWebSocketOutgoingMessage;
import com.gruelbox.orko.websocket.OrkoWebSocketServer;
import com.gruelbox.orko.websocket.OrkoWebsocketStreamingService;
import com.gruelbox.orko.wiring.BackgroundProcessingConfiguration;

import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;

/**
 * Remote websocket-based {@link MarketDataSubscriptionManager} interacting with
 * an {@link OrkoWebSocketServer}.
 */
@VisibleForTesting
public class WSRemoteMarketDataSubscriptionManager extends AbstractMarketDataSubscriptionManager {

  private volatile OrkoWebsocketStreamingService streamingService;
  private volatile Set<MarketDataSubscription> subscriptions = Set.of();

  @Inject
  public WSRemoteMarketDataSubscriptionManager(BackgroundProcessingConfiguration configuration) {
    super(configuration);
  }

  @Override
  protected void doRun() throws InterruptedException {
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
        logger.error("Failed to open connection", e);
        Thread.sleep(10000);
      }
    } while (this.streamingService == null && !Thread.currentThread().isInterrupted() && !isTerminated());

    while (!Thread.currentThread().isInterrupted() && !isTerminated()) {
      suspend("main", getPhase(), false);
    }

    logger.info("Shutting down...");
    if (this.streamingService != null) {
      logger.info("Closing connection");
      try {
        this.streamingService.disconnect();
        logger.info("Connection closed");
      } catch (Exception e) {
        logger.error("Error closing connection", e);
      }
    }
  }

  private void openChannels() {
    ObjectMapper om = StreamingObjectMapperHelper.getObjectMapper();
    this.streamingService.subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.BALANCE)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, BalanceEvent.class))
        .subscribe(balanceOut::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.BALANCE));
    this.streamingService.subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.OPEN_ORDERS)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, OpenOrdersEvent.class))
        .subscribe(openOrdersOut::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.OPEN_ORDERS));
    this.streamingService.subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.ORDER_STATUS_CHANGE)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, OrderChangeEvent.class))
        .subscribe(orderStatusChangeOut::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.ORDER_STATUS_CHANGE));
    this.streamingService.subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.ORDERBOOK)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, OrderBookEvent.class))
        .subscribe(orderbookOut::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.ORDERBOOK));
    this.streamingService.subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.TICKER)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, TickerEvent.class))
        .subscribe(tickersOut::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.TICKER));
    this.streamingService.subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.TRADE)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, TradeEvent.class))
        .subscribe(tradesOut::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.TRADE));
    this.streamingService.subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.USER_TRADE)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, UserTradeEvent.class))
        .subscribe(userTradesOut::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.USER_TRADE));
  }

  private void onError(Throwable e, OrkoWebSocketOutgoingMessage.Nature nature) {
    logger.error("Error in {} stream", nature, e);
  }

  @Override
  public void updateSubscriptions(Set<MarketDataSubscription> subscriptions) {
    this.subscriptions = subscriptions;
    if (this.streamingService != null && this.streamingService.isSocketOpen()) {
      this.streamingService.updateSubscriptions(subscriptions);
    }
  }
}