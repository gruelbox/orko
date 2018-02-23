package com.grahamcrockford.oco.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(use=JsonTypeInfo.Id.CUSTOM, include=JsonTypeInfo.As.PROPERTY, property="jobType")
@JsonTypeIdResolver(JobTypeResolver.class)
public interface Job {
  public String id();
  public JobBuilder<? extends Job> toBuilder();
  public Class<? extends JobProcessor<? extends Job>> processor();
}
