package com.grahamcrockford.oco.wiring;

import java.util.concurrent.ExecutorService;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import io.dropwizard.lifecycle.Managed;

public class WiringModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class).addBinding().to(CommonEnvironmentInitialiser.class);
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(ExecutorServiceManager.class);
  }

  @Provides
  @Singleton
  EventBus eventBus() {
    return new EventBus();
  }

  @Provides
  ExecutorService executor(ExecutorServiceManager managedExecutor) {
    return managedExecutor.executor();
  }
}