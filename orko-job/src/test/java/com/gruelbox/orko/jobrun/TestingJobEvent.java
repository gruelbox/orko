package com.gruelbox.orko.jobrun;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TestingJobEvent {

  public static final TestingJobEvent create(String jobId, EventType eventType) {
    return new AutoValue_TestingJobEvent(jobId, eventType);
  }

  public abstract String jobId();
  public abstract EventType eventType();

  public enum EventType {
    START,
    FINISH
  }
}