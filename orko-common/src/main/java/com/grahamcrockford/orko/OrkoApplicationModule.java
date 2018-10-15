package com.grahamcrockford.orko;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;

import io.dropwizard.setup.Environment;

public class OrkoApplicationModule extends AbstractModule {

  private final ObjectMapper objectMapper;
  private final OrkoConfiguration configuration;
  private final Client jerseyClient;
  private final Environment environment;

  public OrkoApplicationModule(OrkoConfiguration configuration, ObjectMapper objectMapper, Client client, Environment environment) {
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
    bind(OrkoConfiguration.class).toInstance(configuration);
    bind(Client.class).toInstance(jerseyClient);
    bind(Environment.class).toInstance(environment);
  }
}
