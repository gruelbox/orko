package com.gruelbox.orko.allinone;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.auth.AuthModule;
import com.gruelbox.orko.exchange.ExchangeResourceModule;
import com.gruelbox.orko.guardian.GuardianModule;
import com.gruelbox.orko.guardian.InProcessJobSubmitter;
import com.gruelbox.orko.submit.JobSubmitter;
import com.gruelbox.orko.websocket.WebSocketModule;
import com.gruelbox.orko.wiring.EnvironmentInitialiser;

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
    install(new AuthModule(configuration.getAuth()));
    install(new WebSocketModule());
    install(new ExchangeResourceModule());
    bind(JobSubmitter.class).to(InProcessJobSubmitter.class);
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class).addBinding().to(AllInOneEnvironment.class);
  }
}