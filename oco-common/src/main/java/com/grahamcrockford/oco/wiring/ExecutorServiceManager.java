package com.grahamcrockford.oco.wiring;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.inject.Singleton;

import io.dropwizard.lifecycle.Managed;

@Singleton
class ExecutorServiceManager implements Managed {

  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Override
  public void start() throws Exception {
    // Nothing to do
  }

  @Override
  public void stop() throws Exception {
    executor.shutdownNow();
    executor.awaitTermination(30, TimeUnit.SECONDS);
  }

  public ExecutorService executor() {
    return executor;
  }
}