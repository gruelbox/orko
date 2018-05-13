package com.grahamcrockford.oco.marketdata;

import java.util.function.Consumer;

import com.grahamcrockford.oco.spi.TickerSpec;

public interface ExchangeEventRegistry {

  public void registerTicker(TickerSpec spec, String subscriberId, Consumer<TickerEvent> callback);

  public void unregisterTicker(TickerSpec spec, String subscriberId);

  public void changeTickers(Iterable<TickerSpec> targetTickers, String subscriberId, Consumer<TickerEvent> callback);

}