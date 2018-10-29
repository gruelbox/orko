package com.grahamcrockford.orko.allinone;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.grahamcrockford.orko.OrkoApplicationModule;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.auth.AuthModule;
import com.grahamcrockford.orko.auth.EnforceHerokuHttpsModule;
import com.grahamcrockford.orko.exchange.ExchangeResourceModule;
import com.grahamcrockford.orko.guardian.GuardianModule;
import com.grahamcrockford.orko.guardian.InProcessJobSubmitter;
import com.grahamcrockford.orko.submit.JobSubmitter;
import com.grahamcrockford.orko.websocket.WebSocketModule;

import io.dropwizard.setup.Environment;

/**
 * Top level bindings.
 */
class AllInOneModule extends AbstractModule {

  private final OrkoApplicationModule appModule;

  public AllInOneModule(OrkoConfiguration configuration, ObjectMapper objectMapper, Client client, Environment environment) {
    this.appModule = new OrkoApplicationModule(configuration, objectMapper, client, environment);
  }

  @Override
  protected void configure() {
    install(appModule);
    install(new GuardianModule(false));
    install(new AuthModule());
    install(new WebSocketModule());
    install(new ExchangeResourceModule());
    install(new EnforceHerokuHttpsModule());
    bind(JobSubmitter.class).to(InProcessJobSubmitter.class);
  }
}