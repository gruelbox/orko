package com.grahamcrockford.oco.util;

import com.google.inject.Inject;
import com.grahamcrockford.oco.OcoConfiguration;

public class Sleep {

  private final OcoConfiguration configuration;

  @Inject
  public Sleep(OcoConfiguration configuration) {
    this.configuration = configuration;
  }

  public void sleep() throws InterruptedException {
    Thread.sleep(configuration.getLoopSeconds() * 1000);
  }

}
