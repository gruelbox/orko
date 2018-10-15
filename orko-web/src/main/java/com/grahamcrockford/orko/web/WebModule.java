package com.grahamcrockford.orko.web;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.grahamcrockford.orko.OrkoApplicationModule;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.auth.AuthModule;
import com.grahamcrockford.orko.exchange.ExchangeResourceModule;
import com.grahamcrockford.orko.mq.MqModule;
import com.grahamcrockford.orko.websocket.WebSocketModule;

import io.dropwizard.setup.Environment;

/**
 * Top level bindings.
 */
class WebModule extends AbstractModule {

  private final OrkoApplicationModule appModule;

  public WebModule(OrkoConfiguration configuration, ObjectMapper objectMapper, Client client, Environment environment) {
    this.appModule = new OrkoApplicationModule(configuration, objectMapper, client, environment);
  }

  @Override
  protected void configure() {
    install(appModule);
    install(new MqModule());
    install(new AuthModule());
    install(new WebSocketModule());
    install(new ExchangeResourceModule());
  }
}