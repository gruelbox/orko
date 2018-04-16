package com.grahamcrockford.oco.web;

import javax.ws.rs.client.Client;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;
import com.grahamcrockford.oco.CommonModule;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.auth.AuthModule;
import com.grahamcrockford.oco.wiring.WebResource;

/**
 * Top level bindings.
 */
class WebModule extends AbstractModule {

  private final ObjectMapper objectMapper;
  private final OcoConfiguration configuration;
  private final Client jerseyClient;

  public WebModule(OcoConfiguration configuration, ObjectMapper objectMapper, Client client) {
    this.configuration = configuration;
    this.objectMapper = objectMapper;
    this.jerseyClient = client;
  }

  @Override
  protected void configure() {
    install(new CommonModule());
    install(new ServletModule());
    install(new AuthModule());

    bind(ObjectMapper.class).toInstance(objectMapper);
    bind(OcoConfiguration.class).toInstance(configuration);
    bind(Client.class).toInstance(jerseyClient);

    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(JobResource.class);
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(ExchangeResource.class);
    Multibinder.newSetBinder(binder(), HealthCheck.class).addBinding().to(ExchangeAccessHealthCheck.class);
    Multibinder.newSetBinder(binder(), HealthCheck.class).addBinding().to(TickerWebsocketHealthCheck.class);
  }
}