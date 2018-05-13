package com.grahamcrockford.oco.marketdata;

import java.util.function.Consumer;

import com.google.common.collect.Multimap;
import com.grahamcrockford.oco.spi.TickerSpec;

public interface ExchangeEventRegistry {

  public void registerTicker(TickerSpec spec, String subscriberId, Consumer<TickerEvent> callback);

  public void unregisterTicker(TickerSpec spec, String subscriberId);

  public void changeSubscriptions(
      Multimap<TickerSpec, MarketDataType> targetSubscriptions,
      String subscriberId,
      Consumer<TickerEvent> tickerCallback,
      Consumer<OpenOrdersEvent> openOrdersCallback);

}