package com.grahamcrockford.oco.core;

import com.google.inject.Singleton;

@Singleton
public class AdvancedOrderIdGenerator {

  public long next() {
    return System.currentTimeMillis();
  }

}
