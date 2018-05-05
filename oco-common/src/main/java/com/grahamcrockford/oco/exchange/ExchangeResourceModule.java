package com.grahamcrockford.oco.exchange;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.exchange.ExchangeAccessHealthCheck;
import com.grahamcrockford.oco.exchange.ExchangeResource;
import com.grahamcrockford.oco.exchange.JobResource;
import com.grahamcrockford.oco.wiring.WebResource;

public class ExchangeResourceModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(JobResource.class);
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(ExchangeResource.class);
    Multibinder.newSetBinder(binder(), HealthCheck.class).addBinding().to(ExchangeAccessHealthCheck.class);
  }
}