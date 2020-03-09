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
import com.gruelbox.orko.job.LimitOrderJob.BalanceState;
import com.gruelbox.orko.job.LimitOrderJob.Direction;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobBuilder;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.JobProcessor;
import com.gruelbox.orko.spi.TickerSpec;
import java.math.BigDecimal;
import java.util.Optional;
import javax.annotation.Nullable;

@AutoValue
@JsonDeserialize(builder = SoftTrailingStop.Builder.class)
public abstract class SoftTrailingStop implements Job {

  public static final Builder builder() {
    return new AutoValue_SoftTrailingStop.Builder()
        .limitPrice(BigDecimal.ZERO)
        .balanceState(BalanceState.SUFFICIENT_BALANCE);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public abstract static class Builder implements JobBuilder<SoftTrailingStop> {

    @JsonCreator
    private static Builder create() {
      return SoftTrailingStop.builder();
    }

    @Override
    public abstract Builder id(String value);

    public abstract Builder tickTrigger(TickerSpec tickTrigger);

    public abstract Builder amount(BigDecimal amount);

    public abstract Builder direction(Direction direction);

    public abstract Builder startPrice(BigDecimal value);

    public abstract Builder lastSyncPrice(BigDecimal value);

    public abstract Builder stopPrice(BigDecimal value);

    public abstract Builder limitPrice(BigDecimal value);

    abstract Builder balanceState(BalanceState balanceState);

    abstract BigDecimal startPrice();

    abstract Optional<BigDecimal> lastSyncPrice();

    abstract SoftTrailingStop autoBuild();

    @Override
    public SoftTrailingStop build() {
      if (!lastSyncPrice().isPresent()) {
        lastSyncPrice(startPrice());
      }
      return autoBuild();
    }
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Nullable
  public abstract String id();

  @JsonProperty
  public abstract TickerSpec tickTrigger();

  @JsonProperty
  public abstract Direction direction();

  @JsonProperty
  public abstract BigDecimal amount();

  @JsonProperty
  public abstract BigDecimal startPrice();

  @JsonProperty
  public abstract BigDecimal lastSyncPrice();

  @JsonProperty
  public abstract BigDecimal stopPrice();

  @JsonProperty
  public abstract BigDecimal limitPrice();

  @JsonProperty
  abstract BalanceState balanceState();

  @Override
  public String toString() {
    return String.format(
        "soft trailing stop: %s %s at %s on %s",
        amount(), tickTrigger().base(), stopPrice(), tickTrigger());
  }

  @JsonIgnore
  @Override
  public final Class<Processor.ProcessorFactory> processorFactory() {
    return Processor.ProcessorFactory.class;
  }

  public interface Processor extends JobProcessor<SoftTrailingStop> {
    public interface ProcessorFactory extends JobProcessor.Factory<SoftTrailingStop> {
      @Override
      Processor create(SoftTrailingStop job, JobControl jobControl);
    }
  }
}
