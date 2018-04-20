package com.grahamcrockford.oco.web;

import javax.ws.rs.client.Client;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.OcoApplicationModule;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.auth.AuthModule;
import com.grahamcrockford.oco.wiring.WebResource;

/**
 * Top level bindings.
 */
class WebModule extends AbstractModule {

  private final OcoApplicationModule appModule;

  public WebModule(OcoConfiguration configuration, ObjectMapper objectMapper, Client client) {
    this.appModule = new OcoApplicationModule(configuration, objectMapper, client);
  }

  @Override
  protected void configure() {
    install(appModule);
    install(new AuthModule());
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(JobResource.class);
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(ExchangeResource.class);
    Multibinder.newSetBinder(binder(), HealthCheck.class).addBinding().to(ExchangeAccessHealthCheck.class);
    Multibinder.newSetBinder(binder(), HealthCheck.class).addBinding().to(OcoWebsocketHealthCheck.class);
  }
}