package com.grahamcrockford.oco.core;

import java.util.concurrent.ExecutorService;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import io.dropwizard.lifecycle.Managed;

public class CoreModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(ExecutorServiceManager.class);
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(GuardianLoop.class);
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(TickerGenerator.class);
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(MqListener.class);

    bind(TradeServiceFactory.class).to(LiveTradeServiceFactory.class);
    bind(ExchangeEventRegistry.class).to(ExchangeEventBus.class);
  }

  @Provides
  ExecutorService executor(ExecutorServiceManager managedExecutor) {
    return managedExecutor.executor();
  }

  @Provides
  @Singleton
  EventBus eventBus() {
    return new EventBus();
  }

  @Provides
  @Singleton
  AsyncEventBus eventBus(ExecutorService executorService) {
    return new AsyncEventBus(executorService);
  }
}