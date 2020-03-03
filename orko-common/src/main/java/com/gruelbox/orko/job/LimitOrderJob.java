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

/**
 * A job which immediately submits a limit order. Mainly exists to provide a means for more complex
 * orders such as {@link OneCancelsOther} and {@link SoftTrailingStop} to issue trades in a
 * transactional fashion, thus moving the job of working out how to make this task idempotent into
 * one place.
 *
 * @author Graham Crockford
 */
@AutoValue
@JsonDeserialize(builder = LimitOrderJob.Builder.class)
public abstract class LimitOrderJob implements Job {

  public static final Builder builder() {
    return new AutoValue_LimitOrderJob.Builder().balanceState(BalanceState.SUFFICIENT_BALANCE);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public abstract static class Builder implements JobBuilder<LimitOrderJob> {

    @JsonCreator
    private static Builder create() {
      return LimitOrderJob.builder();
    }

    @Override
    public abstract Builder id(String value);

    public abstract Builder tickTrigger(TickerSpec tickTrigger);

    public abstract Builder amount(BigDecimal amount);

    public abstract Builder limitPrice(BigDecimal value);

    public abstract Builder direction(Direction direction);

    abstract Builder balanceState(BalanceState balanceState);

    @Override
    public abstract LimitOrderJob build();
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
  public abstract BigDecimal limitPrice();

  @JsonProperty
  abstract BalanceState balanceState();

  @Override
  public String toString() {
    return String.format(
        "%s order: %s %s at %s on %s",
        direction(), amount(), tickTrigger().base(), limitPrice(), tickTrigger());
  }

  @JsonIgnore
  @Override
  public final Class<Processor.ProcessorFactory> processorFactory() {
    return Processor.ProcessorFactory.class;
  }

  public enum Direction {
    BUY,
    SELL
  }

  public enum BalanceState {
    SUFFICIENT_BALANCE,
    INSUFFICIENT_BALANCE
  }

  public interface Processor extends JobProcessor<LimitOrderJob> {
    public interface ProcessorFactory extends JobProcessor.Factory<LimitOrderJob> {
      @Override
      Processor create(LimitOrderJob job, JobControl jobControl);
    }
  }
}
