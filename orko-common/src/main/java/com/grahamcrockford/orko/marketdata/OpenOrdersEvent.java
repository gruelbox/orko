package com.grahamcrockford.orko.marketdata;

import org.knowm.xchange.dto.trade.OpenOrders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.orko.spi.TickerSpec;

@AutoValue
@JsonDeserialize
public abstract class OpenOrdersEvent {

  @JsonCreator
  public static OpenOrdersEvent create(@JsonProperty("spec") TickerSpec spec,
                                       @JsonProperty("openOrders") OpenOrders openOrders) {
    return new AutoValue_OpenOrdersEvent(spec, openOrders);
  }

  @JsonProperty
  public abstract TickerSpec spec();

  @JsonProperty
  public abstract OpenOrders openOrders();
}