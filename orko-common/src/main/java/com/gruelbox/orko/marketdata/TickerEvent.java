package com.gruelbox.orko.marketdata;

import org.knowm.xchange.dto.marketdata.Ticker;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.gruelbox.orko.spi.TickerSpec;

@AutoValue
@JsonDeserialize
public abstract class TickerEvent {

  @JsonCreator
  public static TickerEvent create(@JsonProperty("spec") TickerSpec spec,
                                   @JsonProperty("ticker") Ticker ticker) {
    return new AutoValue_TickerEvent(spec, ticker);
  }

  @JsonProperty
  public abstract TickerSpec spec();

  @JsonProperty
  public abstract Ticker ticker();
}