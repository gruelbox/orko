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
package com.gruelbox.orko.job;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobBuilder;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.JobProcessor;
import com.gruelbox.orko.notification.Notification;
import javax.annotation.Nullable;

@AutoValue
@JsonDeserialize(builder = Alert.Builder.class)
public abstract class Alert implements Job {

  public static final Builder builder() {
    return new AutoValue_Alert.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public abstract static class Builder implements JobBuilder<Alert> {
    @JsonCreator
    private static Builder create() {
      return Alert.builder();
    }

    @Override
    public abstract Builder id(String value);

    public abstract Builder notification(Notification notification);

    @Override
    public abstract Alert build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Nullable
  public abstract String id();

  @JsonProperty
  public abstract Notification notification();

  @Override
  public String toString() {
    return String.format(
        "send %s '%s'", notification().level().toString().toLowerCase(), notification().message());
  }

  @JsonIgnore
  @Override
  public final Class<Processor.ProcessorFactory> processorFactory() {
    return Processor.ProcessorFactory.class;
  }

  public interface Processor extends JobProcessor<Alert> {
    public interface ProcessorFactory extends JobProcessor.Factory<Alert> {
      @Override
      Processor create(Alert job, JobControl jobControl);
    }
  }
}
