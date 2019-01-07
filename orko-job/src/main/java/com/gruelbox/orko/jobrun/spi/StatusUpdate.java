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

package com.gruelbox.orko.jobrun.spi;


import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

/**
 * An event representing the change in {@link Status} of a job.
 *
 * @author Graham Crockford
 */
@AutoValue
@JsonDeserialize
public abstract class StatusUpdate {

  @JsonCreator
  public static StatusUpdate create(@JsonProperty("requestId") String requestId,
                                    @JsonProperty("status") Status status,
                                    @JsonProperty("payload") Object payload) {
    return new AutoValue_StatusUpdate(requestId, status, payload);
  }

  @JsonProperty
  public abstract String requestId();

  @JsonProperty
  public abstract Status status();

  @Nullable
  @JsonProperty
  public abstract Object payload();
}
