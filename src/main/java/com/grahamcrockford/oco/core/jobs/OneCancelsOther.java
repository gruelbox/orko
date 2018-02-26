package com.grahamcrockford.oco.core.jobs;

import java.math.BigDecimal;

import javax.annotation.Nullable;

import org.mongojack.Id;
import org.mongojack.ObjectId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.core.spi.Job;
import com.grahamcrockford.oco.core.spi.JobBuilder;
import com.grahamcrockford.oco.core.spi.TickerSpec;

@AutoValue
@JsonDeserialize(builder = OneCancelsOther.Builder.class)
public abstract class OneCancelsOther implements Job {

  public static final Builder builder() {
    return new AutoValue_OneCancelsOther.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<OneCancelsOther> {
    @JsonCreator private static Builder create() { return OneCancelsOther.builder(); }
    @Override
    @Id @ObjectId
    public abstract Builder id(String value);
    public abstract Builder tickTrigger(TickerSpec tickTrigger);
    public abstract Builder low(ThresholdAndJob thresholdAndJob);
    public abstract Builder high(ThresholdAndJob thresholdAndJob);

    @Override
    public abstract OneCancelsOther build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Id @ObjectId
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

  @JsonIgnore
  @Override
  public final Class<OneCancelsOtherProcessor.Factory> processorFactory() {
    return OneCancelsOtherProcessor.Factory.class;
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