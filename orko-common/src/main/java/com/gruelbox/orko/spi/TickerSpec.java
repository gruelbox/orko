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
package com.gruelbox.orko.spi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import org.knowm.xchange.currency.CurrencyPair;

/** Represents a specific coin pair on a specific exchange. */
@AutoValue
@JsonDeserialize(builder = TickerSpec.Builder.class)
public abstract class TickerSpec {

  public static Builder builder() {
    return new AutoValue_TickerSpec.Builder();
  }

  public static TickerSpec fromKey(String key) {
    String[] split = key.split("/");
    return builder().exchange(split[0]).counter(split[1]).base(split[2]).build();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public abstract static class Builder {
    @JsonCreator
    private static Builder create() {
      return TickerSpec.builder();
    }

    public abstract Builder exchange(String value);

    public abstract Builder counter(String value);

    public abstract Builder base(String value);

    public abstract TickerSpec build();
  }

  @JsonIgnore
  public abstract Builder toBuilder();

  @JsonProperty
  public abstract String exchange();

  @JsonProperty
  public abstract String counter();

  @JsonProperty
  public abstract String base();

  @JsonIgnore
  public final String pairName() {
    return base() + "/" + counter();
  }

  @Override
  public final String toString() {
    return base() + "/" + counter() + "(" + exchange() + ")";
  }

  @JsonIgnore
  public final String key() {
    return exchange() + "/" + counter() + "/" + base();
  }

  @JsonIgnore
  public final CurrencyPair currencyPair() {
    return new CurrencyPair(base(), counter());
  }
}
