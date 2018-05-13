package com.grahamcrockford.oco.marketdata;

import java.util.EnumSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.spi.TickerSpec;

@AutoValue
@JsonDeserialize
public abstract class MarketDataSubscription {

  @JsonCreator
  public static MarketDataSubscription create(@JsonProperty("spec") TickerSpec spec,
                                              @JsonProperty("types") EnumSet<MarketDataType> types) {
    return new AutoValue_MarketDataSubscription(spec, types);
  }

  @JsonProperty
  public abstract TickerSpec spec();

  @JsonProperty
  public abstract Set<MarketDataType> types();
}