package com.grahamcrockford.oco.web;

import java.util.Collection;

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
                                            @JsonProperty("correlationId") String correlationId,
                                            @JsonProperty("tickers") Collection<TickerSpec> tickers) {
    return new AutoValue_OcoWebSocketIncomingMessage(command, correlationId, tickers);
  }

  @JsonProperty
  abstract Command command();

  @JsonProperty
  @Nullable
  abstract String correlationId();

  @JsonProperty
  abstract Collection<TickerSpec> tickers();

  enum Command {
    CHANGE_TICKERS
  }
}