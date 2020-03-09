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
package com.gruelbox.orko.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.websocket.OrkoWebSocketOutgoingMessage;
import com.gruelbox.orko.websocket.OrkoWebSocketServer;
import com.gruelbox.orko.websocket.OrkoWebsocketStreamingService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.dropwizard.lifecycle.Managed;
import io.reactivex.Observable;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remote websocket-based {@link MarketDataSubscriptionManager} interacting with an {@link
 * OrkoWebSocketServer}.
 */
@VisibleForTesting
@Singleton
public class SubscriptionControllerRemoteImpl implements Managed, SubscriptionController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SubscriptionControllerRemoteImpl.class);

  private final SubscriptionPublisher publisher;
  private final RemoteMarketDataConfiguration configuration;
  private final OrkoWebsocketStreamingService streamingService;
  private volatile Set<MarketDataSubscription> subscriptions = Set.of();
  private volatile boolean disconnected;

  @VisibleForTesting
  @Inject
  public SubscriptionControllerRemoteImpl(
      SubscriptionPublisher publisher, RemoteMarketDataConfiguration configuration) {
    this.publisher = publisher;
    this.configuration = configuration;
    this.publisher.setController(this);
    this.streamingService = new OrkoWebsocketStreamingService(configuration.getWebSocketUri());
  }

  @Override
  public void start() throws Exception {
    LOGGER.info("Opening connection to {}", configuration.getWebSocketUri());
    this.streamingService
        .connect()
        .subscribe(
            () -> {
              LOGGER.info("Connection opened to {}", configuration.getWebSocketUri());
              openChannels();
              this.streamingService.updateSubscriptions(subscriptions);
            },
            t -> {
              if (disconnected) {
                LOGGER.info(
                    "Connection failed to {}. Disconnected and will not re-attempt",
                    configuration.getWebSocketUri());
              } else {
                LOGGER.info(
                    "Connection failed to {}. Scheduling re-attempt in 20s",
                    configuration.getWebSocketUri());
                Observable.timer(20, TimeUnit.SECONDS).subscribe(i -> start());
              }
            });
  }

  @Override
  public void stop() throws Exception {
    LOGGER.info("Closing connection to {}", configuration.getWebSocketUri());
    try {
      disconnected = true;
      this.streamingService
          .disconnect()
          .subscribe(
              () -> LOGGER.info("Connection closed to {}", configuration.getWebSocketUri()),
              t ->
                  LOGGER.error(
                      "Error closing connection to {}", configuration.getWebSocketUri(), t));
    } catch (Exception e) {
      LOGGER.error(
          "Error requesting close of connection to {}", configuration.getWebSocketUri(), e);
    }
  }

  private void openChannels() {
    ObjectMapper om = StreamingObjectMapperHelper.getObjectMapper();
    this.streamingService
        .subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.BALANCE)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, BalanceEvent.class))
        .subscribe(publisher::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.BALANCE));
    this.streamingService
        .subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.OPEN_ORDERS)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, OpenOrdersEvent.class))
        .subscribe(
            publisher::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.OPEN_ORDERS));
    this.streamingService
        .subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.ORDER_STATUS_CHANGE)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, OrderChangeEvent.class))
        .subscribe(
            publisher::emit,
            e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.ORDER_STATUS_CHANGE));
    this.streamingService
        .subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.ORDERBOOK)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, OrderBookEvent.class))
        .subscribe(publisher::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.ORDERBOOK));
    this.streamingService
        .subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.TICKER)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, TickerEvent.class))
        .subscribe(publisher::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.TICKER));
    this.streamingService
        .subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.TRADE)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, SerializableTradeEvent.class))
        .map(SerializableTradeEvent::toTradeEvent)
        .subscribe(publisher::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.TRADE));
    this.streamingService
        .subscribeChannel(OrkoWebSocketOutgoingMessage.Nature.USER_TRADE)
        .map(OrkoWebSocketOutgoingMessage::data)
        .map(o -> om.convertValue(o, SerializableTradeEvent.class))
        .map(SerializableTradeEvent::toUserTradeEvent)
        .subscribe(
            publisher::emit, e -> onError(e, OrkoWebSocketOutgoingMessage.Nature.USER_TRADE));
  }

  private void onError(Throwable e, OrkoWebSocketOutgoingMessage.Nature nature) {
    LOGGER.error("Error in {} stream on {}", nature, configuration.getWebSocketUri(), e);
  }

  @Override
  public void updateSubscriptions(Set<MarketDataSubscription> subscriptions) {
    this.subscriptions = subscriptions;
    if (this.streamingService.isSocketOpen()) {
      this.streamingService.updateSubscriptions(subscriptions);
    } else {
      LOGGER.debug(
          "Not sending subscriptions to {}, socket not ready", configuration.getWebSocketUri());
    }
  }
}
