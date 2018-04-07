package com.grahamcrockford.oco.spi;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(use=JsonTypeInfo.Id.CUSTOM, include=JsonTypeInfo.As.PROPERTY, property="jobType")
@JsonTypeIdResolver(JobTypeResolver.class)
public interface Job {

  String id();

  JobBuilder<? extends Job> toBuilder();

  Class<? extends JobProcessor.Factory<? extends Job>> processorFactory();

  default Optional<Job> onSuccess() {
    return Optional.empty();
  }

  default Optional<Job> onFailure() {
    return Optional.empty();
  }
}