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

package com.gruelbox.orko.jobrun;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobBuilder;
import com.gruelbox.orko.jobrun.spi.JobProcessor;

@AutoValue
@JsonDeserialize(builder = DummyJob.Builder.class)
public abstract class DummyJob implements Job {

  public static final Builder builder() {
    return new AutoValue_DummyJob.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<DummyJob> {

    @JsonCreator private static Builder create() { return DummyJob.builder(); }

    @Override
    public abstract Builder id(String value);

    public abstract Builder stringValue(String value);
    public abstract Builder bigDecimalValue(BigDecimal value);

    @Override
    public abstract DummyJob build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  public abstract String id();

  @JsonProperty
  public abstract String stringValue();

  @JsonProperty
  public abstract BigDecimal bigDecimalValue();

  @Override
  @JsonIgnore
  public Class<? extends JobProcessor.Factory<? extends Job>> processorFactory() {
    throw new UnsupportedOperationException();
  }
}
