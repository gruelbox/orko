package com.grahamcrockford.oco;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;

public class OcoApplicationModule extends AbstractModule {

  private final ObjectMapper objectMapper;
  private final OcoConfiguration configuration;
  private final Client jerseyClient;

  public OcoApplicationModule(OcoConfiguration configuration, ObjectMapper objectMapper, Client client) {
    this.configuration = configuration;
    this.objectMapper = objectMapper;
    this.jerseyClient = client;
  }

  @Override
  protected void configure() {
    install(new ServletModule());
    install(new CommonModule());
    bind(ObjectMapper.class).toInstance(objectMapper);
    bind(OcoConfiguration.class).toInstance(configuration);
    bind(Client.class).toInstance(jerseyClient);
  }
}
