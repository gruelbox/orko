package com.grahamcrockford.orko.auth.ipwhitelisting;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.wiring.WebResource;

public class IpWhitelistingModule extends AbstractModule {
  private final AuthConfiguration auth;

  public IpWhitelistingModule(AuthConfiguration auth) {
    this.auth = auth;
  }

  @Override
  protected void configure() {
    if (auth.getIpWhitelisting() != null && auth.getIpWhitelisting().isEnabled()) {
      Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(IpWhitelistingResource.class);
    }
  }
}