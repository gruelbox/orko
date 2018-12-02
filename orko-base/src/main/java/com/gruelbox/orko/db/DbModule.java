package com.gruelbox.orko.db;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.wiring.EnvironmentInitialiser;

public class DbModule extends AbstractModule {

  private final DbConfiguration configuration;

  public DbModule(DbConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    install(new DatabaseAccessModule(configuration));
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class).addBinding().to(DbEnvironment.class);
  }
}