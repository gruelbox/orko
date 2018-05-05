package com.grahamcrockford.oco.websocket;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonDeserialize
abstract class OcoWebSocketOutgoingMessage {

  @JsonCreator
  static OcoWebSocketOutgoingMessage create(@JsonProperty("nature") Nature nature,
                                            @JsonProperty("correlationId") String correlationId,
                                            @JsonProperty("data") Object data) {
    return new AutoValue_OcoWebSocketOutgoingMessage(nature, correlationId, data);
  }

  @JsonProperty
  abstract Nature nature();

  @JsonProperty
  @Nullable
  abstract String correlationId();

  @JsonProperty
  abstract Object data();

  enum Nature {
    ERROR,
    TICKER,
  }
}