package com.grahamcrockford.oco.marketdata;

import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableSet;
import com.grahamcrockford.oco.spi.TickerSpec;

public interface ExchangeEventRegistry {

  public void registerTicker(TickerSpec spec, String subscriberId, Consumer<TickerEvent> callback);

  public void unregisterTicker(TickerSpec spec, String subscriberId);

  public void changeSubscriptions(
      Set<MarketDataSubscription> targetSubscriptions,
      String subscriberId,
      Consumer<TickerEvent> tickerCallback,
      Consumer<OpenOrdersEvent> openOrdersCallback,
      Consumer<OrderBookEvent> orderBookCallback);

  public default void clearSubscriptions(ImmutableSet<MarketDataSubscription> immutableSet, String subscriberId) {
    changeSubscriptions(ImmutableSet.of(), subscriberId, null, null, null);
  }

}