package com.gruelbox.orko.jobrun;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobBuilder;

@AutoValue
@JsonDeserialize(builder = TestingJob.Builder.class)
public abstract class TestingJob implements Job {

  public static final Builder builder() {
    return new AutoValue_TestingJob.Builder()
        .runAsync(false)
        .update(false)
        .stayResident(false)
        .failOnStart(false)
        .failOnStop(false)
        .failOnTick(false);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<TestingJob> {

    @JsonCreator private static Builder create() { return TestingJob.builder(); }

    @Override
    public abstract Builder id(String value);

    public abstract Builder runAsync(boolean runAsync);
    public abstract Builder stayResident(boolean stayResident);
    public abstract Builder update(boolean update);
    public abstract Builder failOnStart(boolean failOnStart);
    public abstract Builder failOnStop(boolean failOnStop);
    public abstract Builder failOnTick(boolean failOnTick);

    @Override
    public abstract TestingJob build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Nullable
  public abstract String id();

  @JsonProperty
  public abstract boolean runAsync();

  @JsonProperty
  public abstract boolean stayResident();

  @JsonProperty
  public abstract boolean update();

  @JsonProperty
  public abstract boolean failOnStart();

  @JsonProperty
  public abstract boolean failOnStop();

  @JsonProperty
  public abstract boolean failOnTick();

  @JsonIgnore
  @Override
  public final Class<TestingJobProcessor.Factory> processorFactory() {
    return TestingJobProcessor.Factory.class;
  }
}