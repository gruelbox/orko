package com.grahamcrockford.orko.allinone;

import com.google.inject.AbstractModule;
import com.grahamcrockford.orko.auth.AuthModule;
import com.grahamcrockford.orko.auth.EnforceHerokuHttpsModule;
import com.grahamcrockford.orko.exchange.ExchangeResourceModule;
import com.grahamcrockford.orko.guardian.GuardianModule;
import com.grahamcrockford.orko.guardian.InProcessJobSubmitter;
import com.grahamcrockford.orko.submit.JobSubmitter;
import com.grahamcrockford.orko.websocket.WebSocketModule;

/**
 * Top level bindings.
 */
class AllInOneModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new GuardianModule(false));
    install(new AuthModule());
    install(new WebSocketModule());
    install(new ExchangeResourceModule());
    install(new EnforceHerokuHttpsModule());
    bind(JobSubmitter.class).to(InProcessJobSubmitter.class);
  }
}