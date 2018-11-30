package com.gruelbox.orko.job;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.gruelbox.orko.spi.Job;
import com.gruelbox.orko.spi.JobBuilder;
import com.gruelbox.orko.spi.JobProcessor;

@AutoValue
@JsonDeserialize(builder = DummyJob.Builder.class)
public abstract class DummyJob implements Job {

  public static final Builder builder() {
    return new AutoValue_DummyJob.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<DummyJob> {

    @JsonCreator private static Builder create() { return DummyJob.builder(); }

    @Override
    public abstract Builder id(String value);

    public abstract Builder stringValue(String value);
    public abstract Builder bigDecimalValue(BigDecimal value);

    @Override
    public abstract DummyJob build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  public abstract String id();

  @JsonProperty
  public abstract String stringValue();
  
  @JsonProperty
  public abstract BigDecimal bigDecimalValue();
  
  @JsonIgnore
  public Class<? extends JobProcessor.Factory<? extends Job>> processorFactory() {
    throw new UnsupportedOperationException();
  }
}