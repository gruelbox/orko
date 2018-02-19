package com.grahamcrockford.oco.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface Job {
  public String id();
  public JobBuilder<? extends Job> toBuilder();
  public Class<? extends JobProcessor<? extends Job>> processor();
}
