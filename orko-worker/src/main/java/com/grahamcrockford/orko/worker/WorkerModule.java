package com.grahamcrockford.orko.worker;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.grahamcrockford.orko.OrkoApplicationModule;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.guardian.GuardianModule;
import com.grahamcrockford.orko.mq.MqModule;

import io.dropwizard.setup.Environment;

/**
 * Top level bindings.
 */
class WorkerModule extends AbstractModule {

  private final OrkoApplicationModule appModule;

  public WorkerModule(OrkoConfiguration configuration, ObjectMapper objectMapper, Client client, Environment environment) {
    this.appModule = new OrkoApplicationModule(configuration, objectMapper, client, environment);
  }

  @Override
  protected void configure() {
    install(appModule);
    install(new MqModule());
    install(new GuardianModule(true));
  }
}