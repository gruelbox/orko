/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.websocket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.gruelbox.orko.spi.TickerSpec;
import java.util.Collection;
import javax.annotation.Nullable;

@AutoValue
@JsonDeserialize
public abstract class OrkoWebSocketIncomingMessage {

  @JsonCreator
  public static OrkoWebSocketIncomingMessage create(
      @JsonProperty("command") Command command,
      @Nullable @JsonProperty("tickers") Collection<TickerSpec> tickers) {
    return new AutoValue_OrkoWebSocketIncomingMessage(command, tickers);
  }

  @JsonProperty
  public abstract Command command();

  @JsonProperty
  @Nullable
  public abstract Collection<TickerSpec> tickers();

  public enum Command {
    CHANGE_TICKERS,
    CHANGE_OPEN_ORDERS,
    CHANGE_ORDER_BOOK,
    CHANGE_TRADES,
    CHANGE_USER_TRADES,
    CHANGE_BALANCE,
    CHANGE_ORDER_STATUS_CHANGE,
    UPDATE_SUBSCRIPTIONS,

    /**
     * The client should send this every 5 seconds to confirm it is keeping up with the incoming
     * data. If the server doesn't receive this it will stop sending. This may cause the connection
     * to drop in extreme cases, but that's fine, the browser will reconnect when it's able.
     */
    READY
  }
}
