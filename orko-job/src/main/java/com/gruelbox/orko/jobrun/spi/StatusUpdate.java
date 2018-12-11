package com.gruelbox.orko.jobrun.spi;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

/**
 * An event representing the change in {@link Status} of a job.
 *
 * @author Graham Crockford
 */
@AutoValue
@JsonDeserialize
public abstract class StatusUpdate {

  @JsonCreator
  public static StatusUpdate create(@JsonProperty("requestId") String requestId,
                                    @JsonProperty("status") Status status,
                                    @JsonProperty("payload") Object payload) {
    return new AutoValue_StatusUpdate(requestId, status, payload);
  }

  @JsonProperty
  public abstract String requestId();

  @JsonProperty
  public abstract Status status();

  @Nullable
  @JsonProperty
  public abstract Object payload();
}