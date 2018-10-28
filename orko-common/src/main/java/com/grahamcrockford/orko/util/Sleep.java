package com.grahamcrockford.orko.util;

import com.google.inject.Inject;
import com.grahamcrockford.orko.OrkoConfiguration;

public class Sleep {

  private final OrkoConfiguration configuration;

  @Inject
  public Sleep(OrkoConfiguration configuration) {
    this.configuration = configuration;
  }

  public void sleep() throws InterruptedException {
    Thread.sleep((long) configuration.getLoopSeconds() * 1000);
  }

}
