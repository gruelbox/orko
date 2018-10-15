package com.grahamcrockford.orko.job;

import java.math.BigDecimal;

import javax.annotation.Nullable;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.orko.spi.Job;
import com.grahamcrockford.orko.spi.JobBuilder;
import com.grahamcrockford.orko.spi.JobControl;
import com.grahamcrockford.orko.spi.JobProcessor;
import com.grahamcrockford.orko.spi.TickerSpec;

@AutoValue
@JsonDeserialize(builder = OneCancelsOther.Builder.class)
public abstract class OneCancelsOther implements Job {

  public static final Builder builder() {
    return new AutoValue_OneCancelsOther.Builder().verbose(true);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<OneCancelsOther> {
    @JsonCreator private static Builder create() { return OneCancelsOther.builder(); }
    @Override
    @Id
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
  @Id
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
      return String.format("when price drops below %s on %s, execute: %s", low().threshold(), tickTrigger(), low().job());
    } else {
      if (low() == null) {
        return String.format("when price rises above %s on %s, execute: %s", high().threshold(), tickTrigger(), high().job());
      } else {
        return String.format("one-cancels-other (high: %s, low: %s) on %s", high().threshold(), low().threshold(), tickTrigger());
      }
    }
  }

  @JsonIgnore
  @Override
  public final Class<Processor.Factory> processorFactory() {
    return Processor.Factory.class;
  }

  public interface Processor extends JobProcessor<OneCancelsOther> {
    public interface Factory extends JobProcessor.Factory<OneCancelsOther> {
      @Override
      Processor create(OneCancelsOther job, JobControl jobControl);
    }
  }

  @AutoValue
  public static abstract class ThresholdAndJob {

    public static ThresholdAndJob create(BigDecimal threshold, Job job) {
      return new AutoValue_OneCancelsOther_ThresholdAndJob(threshold, job);
    }

    @JsonCreator
    public static ThresholdAndJob createJson(@JsonProperty("thresholdAsString") String threshold, @JsonProperty("job") Job job) {
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