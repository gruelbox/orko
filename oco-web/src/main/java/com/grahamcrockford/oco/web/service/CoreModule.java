package com.grahamcrockford.oco.web.service;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.wiring.EnvironmentInitialiser;
import com.grahamcrockford.oco.wiring.WebResource;

public class CoreModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(JobResource.class);
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(ExchangeResource.class);
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class).addBinding().to(CoreEnvironment.class);
    Multibinder.newSetBinder(binder(), HealthCheck.class).addBinding().to(TickerWebsocketHealthCheck.class);
  }
}