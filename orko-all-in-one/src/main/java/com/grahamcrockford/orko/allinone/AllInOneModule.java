package com.grahamcrockford.orko.allinone;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.auth.AuthModule;
import com.grahamcrockford.orko.exchange.ExchangeResourceModule;
import com.grahamcrockford.orko.guardian.GuardianModule;
import com.grahamcrockford.orko.guardian.InProcessJobSubmitter;
import com.grahamcrockford.orko.submit.JobSubmitter;
import com.grahamcrockford.orko.websocket.WebSocketModule;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

/**
 * Top level bindings.
 */
class AllInOneModule extends AbstractModule {

  private final OrkoConfiguration configuration;

  public AllInOneModule(OrkoConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    install(new GuardianModule(false));
    install(new AuthModule(configuration));
    install(new WebSocketModule());
    install(new ExchangeResourceModule());
    bind(JobSubmitter.class).to(InProcessJobSubmitter.class);
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class).addBinding().to(AllInOneEnvironment.class);
  }
}