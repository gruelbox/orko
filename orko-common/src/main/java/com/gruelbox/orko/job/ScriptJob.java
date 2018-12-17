package com.gruelbox.orko.job;

import java.util.Map;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobBuilder;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.JobProcessor;

/**
 * Provides the context for a Javscript job.
 *
 * TODO add signature to prevent modification of scripts via the database
 *
 * @author Graham Crockford
 */
@AutoValue
@JsonDeserialize(builder = ScriptJob.Builder.class)
public abstract class ScriptJob implements Job {

  public static final Builder builder() {
    return new AutoValue_ScriptJob.Builder().state(ImmutableMap.of());
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public abstract static class Builder implements JobBuilder<ScriptJob> {
    @JsonCreator private static Builder create() { return ScriptJob.builder(); }
    @Override
    public abstract Builder id(String value);
    public abstract Builder name(String name);
    public abstract Builder script(String script);
    public abstract Builder scriptHash(String hash);
    public abstract Builder state(Map<String, String> state);

    @Override
    public abstract ScriptJob build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Nullable
  public abstract String id();

  @JsonProperty
  public abstract String name();

  @JsonProperty
  public abstract String script();

  @JsonProperty
  public abstract String scriptHash();

  @JsonProperty
  public abstract Map<String, String> state();

  @JsonIgnore
  @Override
  public final Class<Processor.ProcessorFactory> processorFactory() {
    return Processor.ProcessorFactory.class;
  }

  public interface Processor extends JobProcessor<ScriptJob> {
    public interface ProcessorFactory extends JobProcessor.Factory<ScriptJob> {
      @Override
      Processor create(ScriptJob job, JobControl jobControl);
    }
  }
}