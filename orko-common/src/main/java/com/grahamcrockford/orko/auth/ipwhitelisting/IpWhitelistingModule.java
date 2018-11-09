package com.grahamcrockford.orko.auth.ipwhitelisting;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.orko.wiring.WebResource;

public class IpWhitelistingModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(IpWhitelistingResource.class);
  }
}