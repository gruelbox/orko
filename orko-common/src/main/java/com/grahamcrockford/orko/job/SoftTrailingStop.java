package com.grahamcrockford.orko.job;

import java.math.BigDecimal;
import java.util.Optional;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.orko.job.LimitOrderJob.Direction;
import com.grahamcrockford.orko.spi.Job;
import com.grahamcrockford.orko.spi.JobBuilder;
import com.grahamcrockford.orko.spi.JobControl;
import com.grahamcrockford.orko.spi.JobProcessor;
import com.grahamcrockford.orko.spi.TickerSpec;

@AutoValue
@JsonDeserialize(builder = SoftTrailingStop.Builder.class)
public abstract class SoftTrailingStop implements Job {

  public static final Builder builder() {
    return new AutoValue_SoftTrailingStop.Builder()
        .limitPrice(BigDecimal.ZERO);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public abstract static class Builder implements JobBuilder<SoftTrailingStop> {

    @JsonCreator private static Builder create() { return SoftTrailingStop.builder(); }

    @Override
    public abstract Builder id(String value);
    public abstract Builder tickTrigger(TickerSpec tickTrigger);
    public abstract Builder amount(BigDecimal amount);
    public abstract Builder direction(Direction direction);
    public abstract Builder startPrice(BigDecimal value);
    public abstract Builder lastSyncPrice(BigDecimal value);
    public abstract Builder stopPrice(BigDecimal value);
    public abstract Builder limitPrice(BigDecimal value);

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

  @JsonProperty public abstract TickerSpec tickTrigger();
  @JsonProperty public abstract Direction direction();
  @JsonProperty public abstract BigDecimal amount();
  @JsonProperty public abstract BigDecimal startPrice();
  @JsonProperty public abstract BigDecimal lastSyncPrice();
  @JsonProperty public abstract BigDecimal stopPrice();
  @JsonProperty public abstract BigDecimal limitPrice();

  @Override
  public String toString() {
    return String.format("soft trailing stop: %s %s at %s on %s", amount(), tickTrigger().base(), stopPrice(), tickTrigger());
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