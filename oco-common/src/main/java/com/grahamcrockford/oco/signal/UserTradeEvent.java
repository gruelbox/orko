package com.grahamcrockford.oco.signal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.marketdata.Trade;
import com.grahamcrockford.oco.spi.TickerSpec;

@AutoValue
@JsonDeserialize
public abstract class UserTradeEvent {

  @JsonCreator
  public static UserTradeEvent create(@JsonProperty("spec") TickerSpec spec,
                                      @JsonProperty("trade") Trade trade) {
    return new AutoValue_UserTradeEvent(spec, trade);
  }

  @JsonProperty
  public abstract TickerSpec spec();

  @JsonProperty
  public abstract Trade trade();
}