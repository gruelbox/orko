package com.grahamcrockford.oco;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.core.CoreModule;
import com.grahamcrockford.oco.resources.ResourcesModule;
import com.kjetland.dropwizard.activemq.ActiveMQBundle;

import io.dropwizard.lifecycle.Managed;

/**
 * Top level bindings.
 */
class OcoModule extends AbstractModule {

  private final ObjectMapper objectMapper;
  private final OcoConfiguration configuration;
  private final Client client;
  private final ActiveMQBundle activeMQBundle;

  public OcoModule(OcoConfiguration configuration, ObjectMapper objectMapper, Client client, ActiveMQBundle activeMQBundle) {
    this.configuration = configuration;
    this.objectMapper = objectMapper;
    this.client = client;
    this.activeMQBundle = activeMQBundle;
  }

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Service.class);
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().toInstance(new BrokerTask());
    install(new CoreModule());
    install(new ResourcesModule());
  }

  @Provides
  ObjectMapper objectMapper() {
    return objectMapper;
  }

  @Provides
  OcoConfiguration config() {
    return configuration;
  }

  @Provides
  Client client() {
    return client;
  }

  @Provides
  ActiveMQBundle mqBundle() {
    return activeMQBundle;
  }
}