package com.grahamcrockford.oco.marketdata;

import java.util.List;

import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.spi.TickerSpec;

@AutoValue
@JsonDeserialize
public abstract class TradeHistoryEvent {

  @JsonCreator
  public static TradeHistoryEvent create(@JsonProperty("spec") TickerSpec spec,
                                         @JsonProperty("openOrders") List<UserTrade> userTrades) {
    return new AutoValue_TradeHistoryEvent(spec, userTrades);
  }

  @JsonProperty
  public abstract TickerSpec spec();

  @JsonProperty
  public abstract List<UserTrade> userTrades();
}