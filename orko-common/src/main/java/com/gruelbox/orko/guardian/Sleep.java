package com.gruelbox.orko.guardian;

import com.google.inject.Inject;
import com.gruelbox.orko.OrkoConfiguration;

class Sleep {

  private final OrkoConfiguration configuration;

  @Inject
  public Sleep(OrkoConfiguration configuration) {
    this.configuration = configuration;
  }

  public void sleep() throws InterruptedException {
    Thread.sleep((long) configuration.getLoopSeconds() * 1000);
  }

}
