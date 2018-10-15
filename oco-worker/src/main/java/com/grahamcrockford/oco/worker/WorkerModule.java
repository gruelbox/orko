package com.grahamcrockford.oco.worker;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.grahamcrockford.oco.OrkoApplicationModule;
import com.grahamcrockford.oco.OrkoConfiguration;
import com.grahamcrockford.oco.guardian.GuardianModule;
import com.grahamcrockford.oco.mq.MqModule;
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