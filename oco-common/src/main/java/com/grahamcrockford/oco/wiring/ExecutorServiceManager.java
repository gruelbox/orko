package com.grahamcrockford.oco.wiring;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import com.google.inject.Singleton;

import io.dropwizard.lifecycle.Managed;

@Singleton
class ExecutorServiceManager implements Managed {

  private final ExecutorService executor;

  ExecutorServiceManager() {
    BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
        .namingPattern("AsyncEventBus-%d")
        .daemon(false)
        .priority(Thread.NORM_PRIORITY)
        .build();
    executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
        30L, TimeUnit.SECONDS,
        new SynchronousQueue<Runnable>(),
        threadFactory);
  }

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