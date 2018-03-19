package com.grahamcrockford.oco.core;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.api.db.DbModule;
import com.grahamcrockford.oco.api.util.EnvironmentInitialiser;
import com.grahamcrockford.oco.core.jobs.JobsModule;
import com.grahamcrockford.oco.core.telegram.TelegramModule;

import io.dropwizard.lifecycle.Managed;

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

    Multibinder.newSetBinder(binder(), Service.class);
    Multibinder.newSetBinder(binder(), Managed.class);
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class);

    bind(ObjectMapper.class).toInstance(objectMapper);
    bind(OcoConfiguration.class).toInstance(configuration);
    bind(Client.class).toInstance(jerseyClient);

    install(new CoreModule());
    install(new DbModule());
    install(new TelegramModule());
    install(new JobsModule());
  }
}