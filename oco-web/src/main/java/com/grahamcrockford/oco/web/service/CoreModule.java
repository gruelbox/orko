package com.grahamcrockford.oco.web.service;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.api.util.EnvironmentInitialiser;
import com.grahamcrockford.oco.web.WebResource;

public class CoreModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(JobResource.class);
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(ExchangeResource.class);
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class).addBinding().to(CoreEnvironment.class);
  }
}