package com.gruelbox.orko.web;

import com.google.inject.AbstractModule;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.auth.AuthModule;
import com.gruelbox.orko.exchange.ExchangeResourceModule;
import com.gruelbox.orko.mq.MqModule;
import com.gruelbox.orko.websocket.WebSocketModule;

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