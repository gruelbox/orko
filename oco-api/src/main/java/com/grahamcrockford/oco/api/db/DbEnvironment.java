package com.grahamcrockford.oco.api.db;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.util.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
class DbEnvironment implements EnvironmentInitialiser {

  private final DatabaseHealthCheck databaseHealthCheck;

  @Inject
  DbEnvironment(DatabaseHealthCheck databaseHealthCheck) {
    this.databaseHealthCheck = databaseHealthCheck;
  }

  @Override
  public void init(Environment environment) {
    environment.healthChecks().register("database", databaseHealthCheck);
  }
}