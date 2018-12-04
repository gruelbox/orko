package com.gruelbox.orko.app.monolith;

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.auth.AuthModule;
import com.gruelbox.orko.exchange.ExchangeResourceModule;
import com.gruelbox.orko.jobrun.InProcessJobSubmitter;
import com.gruelbox.orko.jobrun.JobSubmitter;
import com.gruelbox.orko.websocket.WebSocketModule;
import com.gruelbox.tools.dropwizard.guice.Configured;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;

/**
 * Top level bindings.
 */
class MonolithModule extends AbstractModule implements Configured<OrkoConfiguration> {

  private OrkoConfiguration configuration;

  @Override
  public void setConfiguration(OrkoConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    install(new AuthModule(configuration.getAuth()));
    install(new WebSocketModule());
    install(new ExchangeResourceModule());
    bind(JobSubmitter.class).to(InProcessJobSubmitter.class);
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class)
      .addBinding().to(MonolithEnvironment.class);
  }

  @Provides
  @Named(AuthModule.ROOT_PATH)
  @Singleton
  String rootPath(OrkoConfiguration configuration) {
    return configuration.getRootPath();
  }

  @Provides
  @Named(AuthModule.WEBSOCKET_ENTRY_POINT)
  @Singleton
  String webSocketEntryPoint(OrkoConfiguration configuration) {
    return WebSocketModule.ENTRY_POINT;
  }
}