package com.grahamcrockford.oco.worker;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.grahamcrockford.oco.OcoApplicationModule;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.auth.AuthModule;
import com.grahamcrockford.oco.exchange.ExchangeResourceModule;
import com.grahamcrockford.oco.guardian.GuardianModule;
import com.grahamcrockford.oco.guardian.InProcessJobSubmitter;
import com.grahamcrockford.oco.submit.JobSubmitter;
import com.grahamcrockford.oco.websocket.WebSocketModule;

import io.dropwizard.setup.Environment;

/**
 * Top level bindings.
 */
class AllInOneModule extends AbstractModule {

  private final OcoApplicationModule appModule;

  public AllInOneModule(OcoConfiguration configuration, ObjectMapper objectMapper, Client client, Environment environment) {
    this.appModule = new OcoApplicationModule(configuration, objectMapper, client, environment);
  }

  @Override
  protected void configure() {
    install(appModule);
    install(new GuardianModule(false));
    install(new AuthModule());
    install(new WebSocketModule());
    install(new ExchangeResourceModule());
    bind(JobSubmitter.class).to(InProcessJobSubmitter.class);
  }
}