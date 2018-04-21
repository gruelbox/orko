package com.grahamcrockford.oco;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;

import io.dropwizard.setup.Environment;

public class OcoApplicationModule extends AbstractModule {

  private final ObjectMapper objectMapper;
  private final OcoConfiguration configuration;
  private final Client jerseyClient;
  private final Environment environment;

  public OcoApplicationModule(OcoConfiguration configuration, ObjectMapper objectMapper, Client client, Environment environment) {
    this.configuration = configuration;
    this.objectMapper = objectMapper;
    this.jerseyClient = client;
    this.environment = environment;
  }

  @Override
  protected void configure() {
    install(new ServletModule());
    install(new CommonModule());
    bind(ObjectMapper.class).toInstance(objectMapper);
    bind(OcoConfiguration.class).toInstance(configuration);
    bind(Client.class).toInstance(jerseyClient);
    bind(Environment.class).toInstance(environment);
  }
}
