/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gruelbox.orko.websocket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonDeserialize
abstract class OrkoWebSocketOutgoingMessage {

  @JsonCreator
  static OrkoWebSocketOutgoingMessage create(@JsonProperty("nature") Nature nature,
                                            @JsonProperty("data") Object data) {
    return new AutoValue_OrkoWebSocketOutgoingMessage(nature, data);
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
    USER_TRADE,
    TRADE,
    USER_TRADE_HISTORY,
    BALANCE,
    NOTIFICATION,
    STATUS_UPDATE,
    ORDER_STATUS_CHANGE
  }
}