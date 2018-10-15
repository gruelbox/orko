package com.grahamcrockford.oco.websocket;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.websocket.OrkoWebsocketHealthCheck;


public class WebSocketModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), HealthCheck.class).addBinding().to(OrkoWebsocketHealthCheck.class);
  }
}