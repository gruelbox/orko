package com.gruelbox.orko;

import javax.inject.Inject;

import com.google.inject.Module;
import com.gruelbox.orko.db.DatabaseSetup;
import com.gruelbox.tools.dropwizard.guice.GuiceBundle;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public abstract class BaseApplication extends Application<OrkoConfiguration> {

  @Inject private DatabaseSetup databaseSetup;

  @Override
  public void initialize(final Bootstrap<OrkoConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
      new SubstitutingSourceProvider(
        bootstrap.getConfigurationSourceProvider(),
        new EnvironmentVariableSubstitutor(false)
      )
    );
    bootstrap.addBundle(
      new GuiceBundle<OrkoConfiguration>(
        this,
        new OrkoApplicationModule(),
        createApplicationModule()
      )
    );
  }

  protected abstract Module createApplicationModule();

  @Override
  public void run(final OrkoConfiguration configuration, final Environment environment) {
    databaseSetup.setup();
  }
}