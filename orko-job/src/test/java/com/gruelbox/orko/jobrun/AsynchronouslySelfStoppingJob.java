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
@JsonDeserialize(builder = AsynchronouslySelfStoppingJob.Builder.class)
public abstract class AsynchronouslySelfStoppingJob implements Job {

  public static final Builder builder() {
    return new AutoValue_AsynchronouslySelfStoppingJob.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<AsynchronouslySelfStoppingJob> {

    @JsonCreator private static Builder create() { return AsynchronouslySelfStoppingJob.builder(); }

    @Override
    public abstract Builder id(String value);

    @Override
    public abstract AsynchronouslySelfStoppingJob build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Nullable
  public abstract String id();

  @JsonIgnore
  @Override
  public final Class<AsynchronouslySelfStoppingJobProcessor.Factory> processorFactory() {
    return AsynchronouslySelfStoppingJobProcessor.Factory.class;
  }
}