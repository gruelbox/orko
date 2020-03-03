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
import com.gruelbox.orko.spi.TickerSpec;
import java.math.BigDecimal;
import javax.annotation.Nullable;

@AutoValue
@JsonDeserialize(builder = OneCancelsOther.Builder.class)
public abstract class OneCancelsOther implements Job {

  public static final Builder builder() {
    return new AutoValue_OneCancelsOther.Builder().verbose(true);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public abstract static class Builder implements JobBuilder<OneCancelsOther> {
    @JsonCreator
    private static Builder create() {
      return OneCancelsOther.builder();
    }

    @Override
    public abstract Builder id(String value);

    public abstract Builder tickTrigger(TickerSpec tickTrigger);

    public abstract Builder low(ThresholdAndJob thresholdAndJob);

    public abstract Builder high(ThresholdAndJob thresholdAndJob);

    public abstract Builder verbose(boolean verbose);

    @Override
    public abstract OneCancelsOther build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Nullable
  public abstract String id();

  @Nullable
  @JsonProperty
  public abstract ThresholdAndJob low();

  @Nullable
  @JsonProperty
  public abstract ThresholdAndJob high();

  @JsonProperty
  public abstract TickerSpec tickTrigger();

  @JsonProperty
  public abstract boolean verbose();

  @Override
  public String toString() {
    if (high() == null) {
      return toStringLowOnly();
    } else {
      if (low() == null) {
        return toStringHighOnly();
      } else {
        return toStringHighOnly() + "; " + toStringLowOnly();
      }
    }
  }

  private String toStringHighOnly() {
    return String.format(
        "when price rises above %s on %s, execute: %s",
        high().threshold(), tickTrigger(), high().job());
  }

  private String toStringLowOnly() {
    return String.format(
        "when price drops below %s on %s, execute: %s",
        low().threshold(), tickTrigger(), low().job());
  }

  @JsonIgnore
  @Override
  public final Class<Processor.ProcessorFactory> processorFactory() {
    return Processor.ProcessorFactory.class;
  }

  public interface Processor extends JobProcessor<OneCancelsOther> {
    public interface ProcessorFactory extends JobProcessor.Factory<OneCancelsOther> {
      @Override
      Processor create(OneCancelsOther job, JobControl jobControl);
    }
  }

  @AutoValue
  public abstract static class ThresholdAndJob {

    public static ThresholdAndJob create(BigDecimal threshold, Job job) {
      return new AutoValue_OneCancelsOther_ThresholdAndJob(threshold, job);
    }

    @JsonCreator
    public static ThresholdAndJob createJson(
        @JsonProperty("thresholdAsString") String threshold, @JsonProperty("job") Job job) {
      return new AutoValue_OneCancelsOther_ThresholdAndJob(new BigDecimal(threshold), job);
    }

    @JsonProperty
    public final String thresholdAsString() {
      return threshold().toPlainString();
    }

    @JsonIgnore
    public abstract BigDecimal threshold();

    @JsonProperty
    public abstract Job job();
  }
}
