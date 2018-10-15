package com.grahamcrockford.orko.marketdata;

import java.util.List;

import org.knowm.xchange.dto.trade.UserTrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.orko.spi.TickerSpec;

@AutoValue
@JsonDeserialize
public abstract class TradeHistoryEvent {

  @JsonCreator
  public static TradeHistoryEvent create(@JsonProperty("spec") TickerSpec spec,
                                         @JsonProperty("openOrders") List<UserTrade> trades) {
    return new AutoValue_TradeHistoryEvent(spec, trades);
  }

  @JsonProperty
  public abstract TickerSpec spec();

  @JsonProperty
  public abstract List<UserTrade> trades();
}