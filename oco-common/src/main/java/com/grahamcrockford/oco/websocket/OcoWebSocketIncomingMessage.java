package com.grahamcrockford.oco.websocket;

import java.util.Collection;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.websocket.AutoValue_OcoWebSocketIncomingMessage;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonDeserialize
abstract class OcoWebSocketIncomingMessage {

  @JsonCreator
  static OcoWebSocketIncomingMessage create(@JsonProperty("command") Command command,
                                            @JsonProperty("tickers") Collection<TickerSpec> tickers) {
    return new AutoValue_OcoWebSocketIncomingMessage(command, tickers);
  }

  @JsonProperty
  abstract Command command();

  @JsonProperty
  @Nullable
  abstract Collection<TickerSpec> tickers();

  enum Command {
    CHANGE_TICKERS,
    CHANGE_OPEN_ORDERS,
    CHANGE_ORDER_BOOK,
    UPDATE_SUBSCRIPTIONS
  }
}