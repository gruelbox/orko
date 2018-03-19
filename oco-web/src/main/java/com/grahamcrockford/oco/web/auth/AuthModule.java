package com.grahamcrockford.oco.web.auth;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.web.WebResource;

public class AuthModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(AuthResource.class);
  }
}