package com.grahamcrockford.oco.marketdata;

import org.knowm.xchange.dto.marketdata.OrderBook;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.spi.TickerSpec;

@AutoValue
@JsonDeserialize
public abstract class OrderBookEvent {

  public static OrderBookEvent create(@JsonProperty("spec") TickerSpec spec,
                                      @JsonProperty("orderBook") OrderBook orderBook) {
    return new AutoValue_OrderBookEvent(spec, new ImmutableOrderBook(orderBook));
  }

  @JsonCreator
  public static OrderBookEvent create(@JsonProperty("spec") TickerSpec spec,
                                      @JsonProperty("orderBook") ImmutableOrderBook orderBook) {
    return new AutoValue_OrderBookEvent(spec, orderBook);
  }

  @JsonProperty
  public abstract TickerSpec spec();

  @JsonProperty
  public abstract ImmutableOrderBook orderBook();
}