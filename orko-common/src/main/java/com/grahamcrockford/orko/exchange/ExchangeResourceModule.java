package com.grahamcrockford.orko.exchange;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.orko.wiring.WebResource;

public class ExchangeResourceModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(JobResource.class);
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(ExchangeResource.class);
    Multibinder.newSetBinder(binder(), HealthCheck.class).addBinding().to(ExchangeAccessHealthCheck.class);
  }
}