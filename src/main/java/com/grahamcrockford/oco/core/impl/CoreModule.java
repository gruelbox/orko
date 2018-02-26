package com.grahamcrockford.oco.core.impl;

import java.util.concurrent.ExecutorService;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.WebResource;
import com.grahamcrockford.oco.core.api.ExchangeEventRegistry;
import com.grahamcrockford.oco.core.api.ExchangeService;
import com.grahamcrockford.oco.core.api.JobSubmitter;
import com.grahamcrockford.oco.core.api.TradeServiceFactory;

import io.dropwizard.lifecycle.Managed;

public class CoreModule extends AbstractModule {
  @Override
  protected void configure() {

    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(JobResource.class);
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(ExchangeResource.class);

    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(ExecutorServiceManager.class);
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(GuardianLoop.class);
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(TickerGenerator.class);

    bind(TradeServiceFactory.class).to(LiveTradeServiceFactory.class);
    bind(ExchangeService.class).to(ExchangeServiceImpl.class);
    bind(JobSubmitter.class).to(JobSubmitterImpl.class);
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