package com.grahamcrockford.orko.wiring;

import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.util.concurrent.Service;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;

class CommonEnvironmentInitialiser implements EnvironmentInitialiser {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommonEnvironmentInitialiser.class);

  @Inject private Set<Service> services;
  @Inject private Set<WebResource> webResources;
  @Inject private Set<Managed> managedTasks;
  @Inject private Set<HealthCheck> healthChecks;

  @Override
  public void init(Environment environment) {

    // Any managed tasks
    managedTasks.stream()
      .peek(t -> LOGGER.info("Starting managed task {}", t))
      .forEach(environment.lifecycle()::manage);

    // And any bound services
    services.stream()
      .peek(t -> LOGGER.info("Starting service {}", t))
      .map(ManagedServiceTask::new)
      .forEach(environment.lifecycle()::manage);

    // And any REST resources
    webResources.stream()
      .peek(t -> LOGGER.info("Registering resource {}", t))
      .forEach(environment.jersey()::register);

    // And health checks
    healthChecks.stream()
      .peek(t -> LOGGER.info("Registering health check {}", t))
      .forEach(t -> environment.healthChecks().register(t.getClass().getSimpleName(), t));
  }
}