package com.gruelbox.orko;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.gruelbox.orko.db.DbConfiguration;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;

public class OrkoApplicationModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new ServletModule());
    install(new CommonModule());
  }

  @Provides
  DbConfiguration dbConfiguration(OrkoConfiguration configuration) {
    return configuration.getDatabase();
  }

  @Provides
  ObjectMapper objectMapper(Environment environment) {
    return environment.getObjectMapper();
  }

  @Provides
  @Singleton
  Client jerseyClient(Environment environment, OrkoConfiguration configuration) {
    return new JerseyClientBuilder(environment)
      .using(configuration.getJerseyClientConfiguration())
      .build("client");
  }
}
