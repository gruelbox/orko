package com.grahamcrockford.orko.web;

import com.google.inject.AbstractModule;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.auth.AuthModule;
import com.grahamcrockford.orko.exchange.ExchangeResourceModule;
import com.grahamcrockford.orko.mq.MqModule;
import com.grahamcrockford.orko.websocket.WebSocketModule;

/**
 * Top level bindings.
 */
class WebModule extends AbstractModule {

  private final OrkoConfiguration configuration;

  public WebModule(OrkoConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    install(new MqModule());
    install(new AuthModule(configuration));
    install(new WebSocketModule());
    install(new ExchangeResourceModule());
  }
}