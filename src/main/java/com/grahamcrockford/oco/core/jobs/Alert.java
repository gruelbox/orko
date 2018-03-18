package com.grahamcrockford.oco.core.jobs;

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
@JsonDeserialize(builder = Alert.Builder.class)
public abstract class Alert implements Job {

  public static final Builder builder() {
    return new AutoValue_Alert.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<Alert> {
    @JsonCreator private static Builder create() { return Alert.builder(); }
    @Override
    @Id @ObjectId
    public abstract Builder id(String value);
    public abstract Builder message(String message);
    @Override
    public abstract Alert build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Id @ObjectId
  @Nullable
  public abstract String id();

  @JsonIgnore
  public abstract String message();

  @JsonIgnore
  @Override
  public final Class<AlertProcessor.Factory> processorFactory() {
    return AlertProcessor.Factory.class;
  }
}