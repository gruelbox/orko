package com.grahamcrockford.oco.ticker;

import java.util.function.BiConsumer;
import org.knowm.xchange.dto.marketdata.Ticker;

import com.grahamcrockford.oco.spi.TickerSpec;

public interface ExchangeEventRegistry {

  public void registerTicker(TickerSpec spec, String jobId ,BiConsumer<TickerSpec, Ticker> callback);

  public void unregisterTicker(TickerSpec spec, String jobId);

  public void changeTickers(Iterable<TickerSpec> targetTickers, String jobId, BiConsumer<TickerSpec, Ticker> callback);

}