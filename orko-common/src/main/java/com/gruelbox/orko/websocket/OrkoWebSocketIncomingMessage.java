package com.gruelbox.orko.websocket;

import java.util.Collection;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.gruelbox.orko.spi.TickerSpec;

@AutoValue
@JsonDeserialize
abstract class OrkoWebSocketIncomingMessage {

  @JsonCreator
  static OrkoWebSocketIncomingMessage create(@JsonProperty("command") Command command,
                                            @JsonProperty("tickers") Collection<TickerSpec> tickers) {
    return new AutoValue_OrkoWebSocketIncomingMessage(command, tickers);
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
    CHANGE_TRADES,
    CHANGE_USER_TRADE_HISTORY,
    CHANGE_BALANCE,
    UPDATE_SUBSCRIPTIONS,

    /*
     * The client should send this every 5 seconds to confirm it is keeping up with the
     * incoming data.  If the server doesn't receive this it will stop sending. This
     * may cause the connection to drop in extreme cases, but that's fine, the browser
     * will reconnect when it's able.
     */
    READY
  }
}