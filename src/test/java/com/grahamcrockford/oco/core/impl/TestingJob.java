package com.grahamcrockford.oco.core.impl;

import java.util.concurrent.CountDownLatch;

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

@AutoValue
@JsonDeserialize(builder = TestingJob.Builder.class)
public abstract class TestingJob implements Job {

  public static final Builder builder() {
    return new AutoValue_TestingJob.Builder().runAsync(false).stayResident(false);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<TestingJob> {

    @JsonCreator private static Builder create() { return TestingJob.builder(); }

    @Override
    @Id @ObjectId public abstract Builder id(String value);

    public abstract Builder runAsync(boolean runAsync);
    public abstract Builder stayResident(boolean stayResident);

    public abstract Builder startLatch(CountDownLatch startLatch);
    public abstract Builder completionLatch(CountDownLatch completionLatch);

    @Override
    public abstract TestingJob build();


  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Nullable
  @Id @ObjectId
  public abstract String id();

  public abstract boolean runAsync();
  public abstract boolean stayResident();

  @Nullable
  public abstract CountDownLatch startLatch();

  @Nullable
  public abstract CountDownLatch completionLatch();

  @JsonIgnore
  @Override
  public final Class<TestingJobProcessor.Factory> processorFactory() {
    return TestingJobProcessor.Factory.class;
  }
}