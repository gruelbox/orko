package com.grahamcrockford.oco.websocket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonDeserialize
abstract class OcoWebSocketOutgoingMessage {

  @JsonCreator
  static OcoWebSocketOutgoingMessage create(@JsonProperty("nature") Nature nature,
                                            @JsonProperty("data") Object data) {
    return new AutoValue_OcoWebSocketOutgoingMessage(nature, data);
  }

  @JsonProperty
  abstract Nature nature();

  @JsonProperty
  abstract Object data();

  enum Nature {
    ERROR,
    TICKER,
    OPEN_ORDERS,
    ORDERBOOK,
    TRADE_HISTORY,
    NOTIFICATION
  }
}