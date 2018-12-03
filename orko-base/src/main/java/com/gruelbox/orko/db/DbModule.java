package com.gruelbox.orko.db;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;

public class DbModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new DatabaseAccessModule());
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class).addBinding().to(DbEnvironment.class);
  }
}