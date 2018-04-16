package com.grahamcrockford.oco.api.ticker;

import java.util.function.Consumer;

import org.knowm.xchange.dto.marketdata.Ticker;

import com.grahamcrockford.oco.spi.TickerSpec;

public interface ExchangeEventRegistry {

  public void registerTicker(TickerSpec spec, String jobId, Consumer<Ticker> callback);

  public void unregisterTicker(TickerSpec spec, String jobId);

}
