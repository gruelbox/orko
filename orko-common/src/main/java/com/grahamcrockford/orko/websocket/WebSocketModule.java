package com.grahamcrockford.orko.websocket;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;


public class WebSocketModule extends AbstractModule {

  public static final String ENTRY_POINT = "/ws";

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), HealthCheck.class).addBinding().to(OrkoWebsocketHealthCheck.class);
  }
}