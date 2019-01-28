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

package com.gruelbox.orko.marketdata;

import java.util.Date;

import org.knowm.xchange.dto.Order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.gruelbox.orko.spi.TickerSpec;


@AutoValue
@JsonDeserialize
public abstract class OrderChangeEvent {

  @JsonCreator
  public static OrderChangeEvent create(@JsonProperty("spec") TickerSpec spec,
                                        @JsonProperty("order") Order order,
                                        @JsonProperty("timestamp") Date timestamp) {
    return new AutoValue_OrderChangeEvent(spec, order, timestamp);
  }

  @JsonProperty
  public abstract TickerSpec spec();

  @JsonProperty
  public abstract Order order();

  @JsonProperty
  public abstract Date timestamp();
}