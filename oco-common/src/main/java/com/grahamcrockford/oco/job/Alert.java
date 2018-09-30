package com.grahamcrockford.oco.job;

import javax.annotation.Nullable;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.spi.Job;
import com.grahamcrockford.oco.spi.JobBuilder;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.JobProcessor;

@AutoValue
@JsonDeserialize(builder = Alert.Builder.class)
public abstract class Alert implements Job {

  public static final Builder builder() {
    return new AutoValue_Alert.Builder().message("Alert").level(AlertLevel.INFO);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<Alert> {
    @JsonCreator private static Builder create() { return Alert.builder(); }
    @Override
    @Id
    public abstract Builder id(String value);
    public abstract Builder message(String message);
    public abstract Builder level(AlertLevel level);
    @Override
    public abstract Alert build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Id
  @Nullable
  public abstract String id();

  @JsonProperty
  public abstract String message();

  @JsonProperty
  public abstract AlertLevel level();

  @Override
  public String toString() {
    return String.format("send alert '%s'", message());
  }

  @JsonIgnore
  @Override
  public final Class<Processor.Factory> processorFactory() {
    return Processor.Factory.class;
  }

  public enum AlertLevel {
    INFO,
    ERROR
  }

  public interface Processor extends JobProcessor<Alert> {
    public interface Factory extends JobProcessor.Factory<Alert> {
      @Override
      Processor create(Alert job, JobControl jobControl);
    }
  }
}