package com.grahamcrockford.oco.db;

import java.util.Arrays;
import java.util.Set;

import com.google.inject.Singleton;
import com.grahamcrockford.oco.marketdata.MarketDataSubscription;
import com.grahamcrockford.oco.marketdata.PermanentSubscriptionAccess;
import jersey.repackaged.com.google.common.collect.Sets;

@Singleton
public class InMemoryPermanentSubscriptionAccess implements PermanentSubscriptionAccess {

  private final Set<MarketDataSubscription> subscriptions = Sets.newConcurrentHashSet();

//  private final Set<MarketDataSubscription> subscriptions = Sets.newConcurrentHashSet(ImmutableSet.of(
//    MarketDataSubscription.create(TickerSpec.builder().base("BTC").counter("USD").exchange("bitfinex").build(), MarketDataType.TICKER),
//    MarketDataSubscription.create(TickerSpec.builder().base("BTC").counter("USD").exchange("bitfinex").build(), MarketDataType.ORDERBOOK),
//    MarketDataSubscription.create(TickerSpec.builder().base("BTC").counter("USDT").exchange("binance").build(), MarketDataType.TICKER),
//    MarketDataSubscription.create(TickerSpec.builder().base("BTC").counter("USDT").exchange("binance").build(), MarketDataType.ORDERBOOK)
//  ));

  @Override
  public void add(MarketDataSubscription... subscription) {
    subscriptions.addAll(Arrays.asList(subscription));
  }

  @Override
  public void remove(MarketDataSubscription... subscription) {
    subscriptions.removeAll(Arrays.asList(subscription));
  }

  @Override
  public Set<MarketDataSubscription> all() {
    return subscriptions;
  }
}