package com.grahamcrockford.oco.resources;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.WebResource;

public class ResourcesModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(TradeResource.class);
  }
}
