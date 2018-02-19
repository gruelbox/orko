package com.grahamcrockford.oco.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface AdvancedOrder {
  public String id();
  public AdvancedOrderBuilder toBuilder();
  public Class<? extends AdvancedOrderProcessor<? extends AdvancedOrder>> processor();
}
