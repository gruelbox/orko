package com.grahamcrockford.oco.core.impl;

import org.knowm.xchange.dto.marketdata.Ticker;

import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.core.spi.TickerSpec;

@AutoValue
abstract class TickerEvent {

  public static TickerEvent create(TickerSpec spec, Ticker ticker) {
    return new AutoValue_TickerEvent(spec, ticker);
  }

  public abstract TickerSpec spec();
  public abstract Ticker ticker();
}