package com.grahamcrockford.oco.worker;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.grahamcrockford.oco.OcoApplicationModule;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.guardian.GuardianModule;
import com.grahamcrockford.oco.telegram.TelegramModule;

/**
 * Top level bindings.
 */
class WorkerModule extends AbstractModule {

  private final OcoApplicationModule appModule;

  public WorkerModule(OcoConfiguration configuration, ObjectMapper objectMapper, Client client) {
    this.appModule = new OcoApplicationModule(configuration, objectMapper, client);
  }

  @Override
  protected void configure() {
    install(appModule);
    install(new TelegramModule());
    install(new GuardianModule());
  }
}