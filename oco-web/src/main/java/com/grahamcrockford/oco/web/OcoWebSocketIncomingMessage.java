package com.grahamcrockford.oco.web;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonDeserialize
abstract class OcoWebSocketIncomingMessage {

  @JsonCreator
  static OcoWebSocketIncomingMessage create(@JsonProperty("command") Command command,
                                            @JsonProperty("accessToken") String accessToken,
                                            @JsonProperty("correlationId") String correlationId,
                                            @JsonProperty("ticker") TickerSpec ticker) {
    return new AutoValue_OcoWebSocketIncomingMessage(command, accessToken, correlationId, ticker);
  }

  @JsonProperty
  abstract Command command();

  @JsonProperty
  @Nullable // Not really, but we want that detected by the authenticator, not here
  abstract String accessToken();

  @JsonProperty
  @Nullable
  abstract String correlationId();

  @JsonProperty
  abstract TickerSpec ticker();

  enum Command {
    START_TICKER,
    STOP_TICKER
  }
}