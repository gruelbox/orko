package com.grahamcrockford.oco.util;

import com.google.inject.Inject;
import com.grahamcrockford.oco.OrkoConfiguration;

public class Sleep {

  private final OrkoConfiguration configuration;

  @Inject
  public Sleep(OrkoConfiguration configuration) {
    this.configuration = configuration;
  }

  public void sleep() throws InterruptedException {
    Thread.sleep(configuration.getLoopSeconds() * 1000);
  }

}
