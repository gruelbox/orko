package com.grahamcrockford.oco.core.api;

import java.util.function.Consumer;

import org.knowm.xchange.dto.marketdata.Ticker;

import com.grahamcrockford.oco.core.spi.TickerSpec;

public interface ExchangeEventRegistry {

  public void registerTicker(TickerSpec spec, String jobId, Consumer<Ticker> callback);

  public void unregisterTicker(TickerSpec spec, String jobId);

}
