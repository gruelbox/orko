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

import com.fasterxml.jackson.databind.JsonNode;
import com.gruelbox.orko.exchange.MarketDataSubscription;
import com.gruelbox.orko.exchange.MarketDataType;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.SafelyDispose;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Maintains a client websocket to an {@link OrkoWebSocketServer} */
public class OrkoWebsocketStreamingService extends JsonNettyStreamingService {

  private static final Logger LOG = LoggerFactory.getLogger(OrkoWebsocketStreamingService.class);
  private final String apiUrl;

  private Disposable interval;
  private volatile Set<MarketDataSubscription> currentSubscriptions = Set.of();

  /**
   * Opens the
   *
   * @param apiUrl
   */
  public OrkoWebsocketStreamingService(String apiUrl) {
    super(apiUrl, 2147483647);
    this.apiUrl = apiUrl;
  }

  @Override
  public Completable connect() {
    Completable completable = super.connect();
    return completable.doOnComplete(
        () -> {
          LOG.info("Connected to {}", apiUrl);
          this.interval =
              Observable.interval(2, TimeUnit.SECONDS)
                  .subscribe(
                      i -> sendMessage(OrkoWebSocketIncomingMessage.Command.READY, Set.of()));
        });
  }

  @Override
  public Completable disconnect() {
    LOG.info("Disconnecting from {}", apiUrl);
    SafelyDispose.of(interval);
    return super.disconnect();
  }

  public synchronized void updateSubscriptions(final Set<MarketDataSubscription> newSubscriptions) {
    if (!currentSubscriptions.equals(newSubscriptions)) {
      sendSubscriptions(newSubscriptions);
      this.currentSubscriptions = Set.copyOf(newSubscriptions);
    } else {
      LOG.info("No subscriptions on {}", apiUrl);
    }
  }

  private void sendSubscriptions(Set<MarketDataSubscription> subscriptions) {
    LOG.debug("Sending {} resubscriptions on {}", subscriptions.size(), apiUrl);
    sendSubscription(subscriptions, MarketDataType.TICKER, CHANGE_TICKERS);
    sendSubscription(subscriptions, BALANCE, CHANGE_BALANCE);
    sendSubscription(subscriptions, OPEN_ORDERS, CHANGE_OPEN_ORDERS);
    sendSubscription(subscriptions, ORDER, CHANGE_ORDER_STATUS_CHANGE);
    sendSubscription(subscriptions, MarketDataType.ORDERBOOK, CHANGE_ORDER_BOOK);
    sendSubscription(subscriptions, TRADES, CHANGE_TRADES);
    sendSubscription(subscriptions, USER_TRADE, CHANGE_USER_TRADES);
    sendMessage(UPDATE_SUBSCRIPTIONS, null);
    LOG.debug("{} resubscriptions sent on {}", subscriptions.size(), apiUrl);
  }

  private void sendSubscription(
      Set<MarketDataSubscription> subscriptions,
      MarketDataType marketDataType,
      OrkoWebSocketIncomingMessage.Command command) {
    sendMessage(
        command,
        subscriptions.stream()
            .filter(it -> it.type() == marketDataType)
            .map(MarketDataSubscription::spec)
            .collect(toSet()));
  }

  private void sendMessage(
      OrkoWebSocketIncomingMessage.Command command, Collection<TickerSpec> tickers) {
    sendObjectMessage(OrkoWebSocketIncomingMessage.create(command, tickers));
  }

  protected String getChannelNameFromMessage(JsonNode message) {
    return message.get("nature").asText();
  }

  @Override
  public String getSubscribeMessage(String s, Object... objects) throws IOException {
    return null;
  }

  @Override
  public String getUnsubscribeMessage(String s) throws IOException {
    return null;
  }

  @Override
  public void resubscribeChannels() {
    sendSubscriptions(currentSubscriptions);
  }

  public Observable<OrkoWebSocketOutgoingMessage> subscribeChannel(
      OrkoWebSocketOutgoingMessage.Nature nature) {
    return super.subscribeChannel(nature.toString())
        .map(json -> this.objectMapper.treeToValue(json, OrkoWebSocketOutgoingMessage.class));
  }
}
