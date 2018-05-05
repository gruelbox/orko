package com.grahamcrockford.oco.worker;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.grahamcrockford.oco.OcoApplicationModule;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.guardian.GuardianModule;
import com.grahamcrockford.oco.mq.MqModule;
import io.dropwizard.setup.Environment;

/**
 * Top level bindings.
 */
class WorkerModule extends AbstractModule {

  private final OcoApplicationModule appModule;

  public WorkerModule(OcoConfiguration configuration, ObjectMapper objectMapper, Client client, Environment environment) {
    this.appModule = new OcoApplicationModule(configuration, objectMapper, client, environment);
  }

  @Override
  protected void configure() {
    install(appModule);
    install(new MqModule());
    install(new GuardianModule(true));
  }
}