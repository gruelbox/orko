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
package com.gruelbox.orko.exchange;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.gruelbox.orko.spi.TickerSpec;

@AutoValue
@JsonDeserialize
public abstract class MarketDataSubscription {

  @JsonCreator
  public static MarketDataSubscription create(
      @JsonProperty("spec") TickerSpec spec, @JsonProperty("type") MarketDataType type) {
    return new AutoValue_MarketDataSubscription(spec, type);
  }

  @JsonProperty
  public abstract TickerSpec spec();

  @JsonProperty
  public abstract MarketDataType type();

  @JsonIgnore
  public final String key() {
    return spec().key() + "/" + type();
  }

  @Override
  public final String toString() {
    return key();
  }
}
