package com.grahamcrockford.orko.marketdata;

import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableSet;
import com.grahamcrockford.orko.spi.TickerSpec;

import io.reactivex.Flowable;

public interface ExchangeEventRegistry {

  public default void changeSubscriptions(String subscriberId, MarketDataSubscription... targetSubscriptions) {
    changeSubscriptions(subscriberId, ImmutableSet.copyOf(targetSubscriptions));
  }

  public void changeSubscriptions(String subscriberId, Set<MarketDataSubscription> targetSubscriptions);

  public default void clearSubscriptions(String subscriberId) {
    changeSubscriptions(subscriberId, ImmutableSet.of());
  }

  public Flowable<TickerEvent> getTickers(String subscriberId);
  public Iterable<Flowable<TickerEvent>> getTickersSplit(String subscriberId);

  public Flowable<OpenOrdersEvent> getOpenOrders(String subscriberId);
  public Flowable<OrderBookEvent> getOrderBooks(String subscriberId);
  public Flowable<TradeEvent> getTrades(String subscriberId);

  public Flowable<TradeHistoryEvent> getUserTradeHistory(String subscriberId);
  public Iterable<Flowable<TradeHistoryEvent>> getUserTradeHistorySplit(String eventRegistryClientId);

  public Flowable<BalanceEvent> getBalance(String subscriberId);

  @Deprecated
  public void registerTicker(TickerSpec tickerSpec, String subscriberId, Consumer<TickerEvent> callback);

  @Deprecated
  public void unregisterTicker(TickerSpec tickerSpec, String subscriberId);


}