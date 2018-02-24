package com.grahamcrockford.oco.resources;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.WebResource;

public class ResourcesModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder<WebResource> resources = Multibinder.newSetBinder(binder(), WebResource.class);
    resources.addBinding().to(ExchangeResource.class);
    resources.addBinding().to(JobResource.class);
    resources.addBinding().to(AuthResource.class);
  }
}
