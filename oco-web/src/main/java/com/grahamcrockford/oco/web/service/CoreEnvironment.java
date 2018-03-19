package com.grahamcrockford.oco.web.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.util.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
class CoreEnvironment implements EnvironmentInitialiser {

  private final ExchangeAccessHealthCheck coreHealthCheck;

  @Inject
  CoreEnvironment(ExchangeAccessHealthCheck coreHealthCheck) {
    this.coreHealthCheck = coreHealthCheck;
  }

  @Override
  public void init(Environment environment) {
    environment.healthChecks().register("core", coreHealthCheck);
  }
}