package com.grahamcrockford.oco.db;

import javax.annotation.Nullable;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.spi.Job;

/**
 * Envelope used for storage of jobs in database, allowing us to remember previously submitted jobs
 * and status.
 */
@AutoValue
abstract class Envelope {

  @JsonCreator
  public static final Envelope create(@JsonProperty("id") @Id String id,
                                      @JsonProperty("job") Job job,
                                      @JsonProperty("processed") boolean processed) {
    return new AutoValue_Envelope(id, job, processed);
  }

  public static final Envelope live(Job job) {
    return new AutoValue_Envelope(job.id(), job, false);
  }

  public static final Envelope dead(String jobId) {
    return new AutoValue_Envelope(jobId, null, true);
  }

  @JsonProperty
  @Id
  public abstract String id();

  @JsonProperty
  @Nullable
  public abstract Job job();

  @JsonProperty
  public abstract boolean processed();
}