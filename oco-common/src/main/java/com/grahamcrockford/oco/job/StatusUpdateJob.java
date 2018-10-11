package com.grahamcrockford.oco.job;

import javax.annotation.Nullable;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.notification.StatusUpdate;
import com.grahamcrockford.oco.spi.Job;
import com.grahamcrockford.oco.spi.JobBuilder;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.JobProcessor;

@AutoValue
@JsonDeserialize(builder = StatusUpdateJob.Builder.class)
public abstract class StatusUpdateJob implements Job {

  public static final Builder builder() {
    return new AutoValue_StatusUpdateJob.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<StatusUpdateJob> {
    @JsonCreator private static Builder create() { return StatusUpdateJob.builder(); }
    @Override
    @Id
    public abstract Builder id(String value);
    public abstract Builder statusUpdate(StatusUpdate statusUpdate);
    @Override
    public abstract StatusUpdateJob build();
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
  public abstract StatusUpdate statusUpdate();

  @Override
  public String toString() {
    return String.format("send status update: %s", statusUpdate());
  }

  @JsonIgnore
  @Override
  public final Class<Processor.Factory> processorFactory() {
    return Processor.Factory.class;
  }

  public interface Processor extends JobProcessor<StatusUpdateJob> {
    public interface Factory extends JobProcessor.Factory<StatusUpdateJob> {
      @Override
      Processor create(StatusUpdateJob job, JobControl jobControl);
    }
  }
}