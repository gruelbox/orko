package com.grahamcrockford.oco.marketdata;

import org.knowm.xchange.dto.marketdata.Trade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.spi.TickerSpec;

@AutoValue
@JsonDeserialize
public abstract class TradeEvent {

  @JsonCreator
  public static TradeEvent create(@JsonProperty("spec") TickerSpec spec,
                                  @JsonProperty("trade") Trade trade) {
    return new AutoValue_TradeEvent(spec, trade);
  }

  @JsonProperty
  public abstract TickerSpec spec();

  @JsonProperty
  public abstract Trade trade();
}