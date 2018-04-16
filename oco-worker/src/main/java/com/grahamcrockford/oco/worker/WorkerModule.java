package com.grahamcrockford.oco.worker;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.guardian.GuardianModule;
import com.grahamcrockford.oco.job.JobsModule;
import com.grahamcrockford.oco.telegram.TelegramModule;

/**
 * Top level bindings.
 */
class WorkerModule extends AbstractModule {

  private final ObjectMapper objectMapper;
  private final OcoConfiguration configuration;
  private final Client jerseyClient;

  public WorkerModule(OcoConfiguration configuration, ObjectMapper objectMapper, Client client) {
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

    install(new GuardianModule());
    install(new TelegramModule());
    install(new JobsModule());
  }
}