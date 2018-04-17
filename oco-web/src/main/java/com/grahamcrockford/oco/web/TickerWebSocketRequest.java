package com.grahamcrockford.oco.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonDeserialize
abstract class TickerWebSocketRequest {

  @JsonCreator
  static TickerWebSocketRequest create(@JsonProperty("command") Command command, @JsonProperty("ticker") TickerSpec ticker) {
    return new AutoValue_TickerWebSocketRequest(command, ticker);
  }

  @JsonProperty
  abstract Command command();

  @JsonProperty
  abstract TickerSpec ticker();

  enum Command {
    START,
    STOP
  }
}