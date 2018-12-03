package com.gruelbox.orko.web;

import com.google.inject.AbstractModule;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.auth.AuthModule;
import com.gruelbox.orko.exchange.ExchangeResourceModule;
import com.gruelbox.orko.mq.MqModule;
import com.gruelbox.orko.websocket.WebSocketModule;
import com.gruelbox.tools.dropwizard.guice.Configured;

/**
 * Top level bindings.
 */
class WebModule extends AbstractModule implements Configured<OrkoConfiguration> {

  private OrkoConfiguration configuration;

  @Override
  public void setConfiguration(OrkoConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    install(new MqModule());
    install(new AuthModule(configuration.getAuth()));
    install(new WebSocketModule());
    install(new ExchangeResourceModule());
  }
}