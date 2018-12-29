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
@JsonDeserialize(builder = CounterJob.Builder.class)
public abstract class CounterJob implements Job {

  public static final Builder builder() {
    return new AutoValue_CounterJob.Builder().counter(1);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<CounterJob> {

    @JsonCreator private static Builder create() { return CounterJob.builder(); }

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