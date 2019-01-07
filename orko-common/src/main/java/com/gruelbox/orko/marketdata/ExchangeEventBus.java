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

package com.gruelbox.orko.marketdata;

import static com.gruelbox.orko.marketdata.MarketDataType.BALANCE;
import static com.gruelbox.orko.marketdata.MarketDataType.OPEN_ORDERS;
import static com.gruelbox.orko.marketdata.MarketDataType.ORDERBOOK;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static com.gruelbox.orko.marketdata.MarketDataType.TRADES;
import static com.gruelbox.orko.marketdata.MarketDataType.USER_TRADE_HISTORY;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.spi.TickerSpec;

import io.reactivex.Flowable;


@Singleton
class ExchangeEventBus implements ExchangeEventRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeEventBus.class);

  private final ConcurrentMap<MarketDataSubscription, AtomicInteger> allSubscriptions = Maps.newConcurrentMap();
  private final MarketDataSubscriptionManager marketDataSubscriptionManager;

  @Inject
  ExchangeEventBus(MarketDataSubscriptionManager marketDataSubscriptionManager) {
    this.marketDataSubscriptionManager = marketDataSubscriptionManager;
  }

  @Override
  public ExchangeEventSubscription subscribe(Set<MarketDataSubscription> targetSubscriptions) {
    SubscriptionImpl subscription = new SubscriptionImpl(targetSubscriptions);
    LOGGER.debug("Created subscriber {} with subscriptions {}", subscription.name, targetSubscriptions);
    return subscription;
  }

  private final class SubscriptionImpl implements ExchangeEventRegistry.ExchangeEventSubscription {

    private final Set<MarketDataSubscription> subscriptions;
    private final String name;

    SubscriptionImpl(Set<MarketDataSubscription> subscriptions) {
      this.subscriptions = subscriptions;
      this.name = UUID.randomUUID().toString();
      if (subscribeAll())
        updateSubscriptions();
    }

    @Override
    public Flowable<TickerEvent> getTickers() {
      Set<TickerSpec> filtered = subscriptionsFor(TICKER);
      return marketDataSubscriptionManager.getTickers()
          .filter(e -> filtered.contains(e.spec()))
          .onBackpressureLatest();
    }

    @Override
    public Iterable<Flowable<TickerEvent>> getTickersSplit() {
      return FluentIterable
          .from(subscriptionsFor(TICKER))
          .transform(spec -> marketDataSubscriptionManager.getTickers()
              .filter(e -> e.spec().equals(spec))
              .onBackpressureLatest());
    }

    @Override
    public Flowable<OpenOrdersEvent> getOpenOrders() {
      Set<TickerSpec> filtered = subscriptionsFor(OPEN_ORDERS);
      return marketDataSubscriptionManager.getOpenOrders()
          .filter(e -> filtered.contains(e.spec()))
          .onBackpressureLatest();
    }

    @Override
    public Flowable<OrderBookEvent> getOrderBooks() {
      Set<TickerSpec> filtered = subscriptionsFor(ORDERBOOK);
      return marketDataSubscriptionManager.getOrderBooks()
          .filter(e -> filtered.contains(e.spec()))
          .onBackpressureLatest();
    }

    @Override
    public Flowable<TradeEvent> getTrades() {
      Set<TickerSpec> filtered = subscriptionsFor(TRADES);
      return marketDataSubscriptionManager.getTrades()
          .filter(e -> filtered.contains(e.spec()))
          .onBackpressureLatest();
    }

    @Override
    public Flowable<TradeHistoryEvent> getUserTradeHistory() {
      Set<TickerSpec> filtered = subscriptionsFor(USER_TRADE_HISTORY);
      return marketDataSubscriptionManager.getUserTradeHistory()
          .filter(e -> filtered.contains(e.spec()))
          .onBackpressureLatest();
    }

    @Override
    public Iterable<Flowable<TradeHistoryEvent>> getUserTradeHistorySplit() {
      return FluentIterable
          .from(subscriptionsFor(USER_TRADE_HISTORY))
          .transform(spec -> marketDataSubscriptionManager.getUserTradeHistory()
              .filter(e -> e.spec().equals(spec))
              .onBackpressureLatest());
    }

    @Override
    public Flowable<BalanceEvent> getBalance() {
      ImmutableSet<String> exchangeCurrenciesSubscribed = FluentIterable.from(subscriptionsFor(BALANCE))
        .transformAndConcat(s -> ImmutableSet.of(s.exchange() + "/" + s.base(), s.exchange() + "/" + s.counter()))
        .toSet();
      return marketDataSubscriptionManager.getBalances()
          .filter(e -> exchangeCurrenciesSubscribed.contains(e.exchange() + "/" + e.currency()))
          .onBackpressureLatest();
    }

    @Override
    public void close() {
      if (unsubscribeAll())
        updateSubscriptions();
    }

    private void updateSubscriptions() {
      marketDataSubscriptionManager.updateSubscriptions(allSubscriptions.keySet());
    }

    private boolean unsubscribeAll() {
      boolean updated = false;
      for (MarketDataSubscription sub : subscriptions) {
        if (unsubscribe(sub))
          updated = true;
      }
      return updated;
    }

    private boolean subscribeAll() {
      boolean updated = false;
      for (MarketDataSubscription sub : subscriptions) {
        if (subscribe(sub))
          updated = true;
      }
      return updated;
    }

    private boolean subscribe(MarketDataSubscription subscription) {
      LOGGER.debug("... subscribing {}", subscription);
      boolean newGlobally = allSubscriptions.computeIfAbsent(subscription, s -> new AtomicInteger(0)).incrementAndGet() == 1;
      if (newGlobally)
        LOGGER.debug("   ... new global subscription");
      return newGlobally;
    }

    private boolean unsubscribe(MarketDataSubscription subscription) {
      LOGGER.debug("... unsubscribing {}", subscription);
      AtomicInteger refCount = allSubscriptions.get(subscription);
      if (refCount == null) {
        LOGGER.warn("   ... Refcount is unset for live subscription: {}", subscription);
        return true;
      }
      int newRefCount = refCount.decrementAndGet();
      LOGGER.debug("   ... refcount set to {}", newRefCount);
      if (newRefCount == 0) {
        LOGGER.debug("   ... removing global subscription");
        allSubscriptions.remove(subscription);
        return true;
      } else {
        LOGGER.debug("   ... other subscribers still holding it open");
        return false;
      }
    }

    private Set<TickerSpec> subscriptionsFor(MarketDataType marketDataType) {
      return FluentIterable.from(subscriptions)
          .filter(s -> s.type().equals(marketDataType))
          .transform(MarketDataSubscription::spec)
          .toSet();
    }

    @Override
    public ExchangeEventSubscription replace(Set<MarketDataSubscription> targetSubscriptions) {
      if (targetSubscriptions.equals(subscriptions))
        return this;
      unsubscribeAll();
      return new SubscriptionImpl(targetSubscriptions);
    }
  }
}