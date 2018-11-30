package com.gruelbox.orko.marketdata;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.reactivex.Flowable;

public interface ExchangeEventRegistry {

  public ExchangeEventSubscription subscribe(Set<MarketDataSubscription> targetSubscriptions);

  public default ExchangeEventSubscription subscribe(MarketDataSubscription... targetSubscriptions) {
    return subscribe(ImmutableSet.copyOf(targetSubscriptions));
  }

  public interface ExchangeEventSubscription extends AutoCloseable {
    Flowable<TickerEvent> getTickers();
    Flowable<OpenOrdersEvent> getOpenOrders();
    Flowable<OrderBookEvent> getOrderBooks();
    Flowable<TradeEvent> getTrades();
    Flowable<TradeHistoryEvent> getUserTradeHistory();
    Flowable<BalanceEvent> getBalance();

    Iterable<Flowable<TickerEvent>> getTickersSplit();
    Iterable<Flowable<TradeHistoryEvent>> getUserTradeHistorySplit();

    public ExchangeEventSubscription replace(Set<MarketDataSubscription> targetSubscriptions);

    public default ExchangeEventSubscription replace(MarketDataSubscription... targetSubscriptions) {
      return replace(ImmutableSet.copyOf(targetSubscriptions));
    }

    @Override
    void close();
  }
}