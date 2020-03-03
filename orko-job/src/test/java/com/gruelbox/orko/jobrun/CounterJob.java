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
package com.gruelbox.orko.jobrun;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobBuilder;
import javax.annotation.Nullable;

@AutoValue
@JsonDeserialize(builder = CounterJob.Builder.class)
public abstract class CounterJob implements Job {

  public static final Builder builder() {
    return new AutoValue_CounterJob.Builder().counter(1);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public abstract static class Builder implements JobBuilder<CounterJob> {

    @JsonCreator
    private static Builder create() {
      return CounterJob.builder();
    }

    @Override
    public abstract Builder id(String value);

    public abstract Builder counter(int value);

    @Override
    public abstract CounterJob build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Nullable
  public abstract String id();

  @JsonProperty
  public abstract int counter();

  @JsonIgnore
  @Override
  public final Class<CounterJobProcessor.Factory> processorFactory() {
    return CounterJobProcessor.Factory.class;
  }
}
