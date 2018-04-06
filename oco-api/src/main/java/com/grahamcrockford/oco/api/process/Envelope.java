package com.grahamcrockford.oco.api.process;

import javax.annotation.Nullable;

import org.mongojack.Id;
import org.mongojack.ObjectId;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.spi.Job;

@AutoValue
public abstract class Envelope {

  public static final Envelope live(Job job) {
    return new AutoValue_Envelope(job.id(), job, false);
  }

  public static final Envelope dead(String jobId) {
    return new AutoValue_Envelope(jobId, null, true);
  }

  @JsonProperty
  @Id @ObjectId
  public abstract String id();

  @JsonProperty
  @Nullable
  public abstract Job job();

  @JsonProperty
  public abstract boolean processed();
}