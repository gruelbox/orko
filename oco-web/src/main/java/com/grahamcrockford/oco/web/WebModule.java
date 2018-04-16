package com.grahamcrockford.oco.web;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.web.auth.AuthModule;
import com.grahamcrockford.oco.web.service.CoreModule;

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
    install(new ServletModule());

    bind(ObjectMapper.class).toInstance(objectMapper);
    bind(OcoConfiguration.class).toInstance(configuration);
    bind(Client.class).toInstance(jerseyClient);

    install(new CoreModule());
    install(new AuthModule());
  }
}