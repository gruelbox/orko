package com.grahamcrockford.oco.web;

import java.util.Set;

import javax.inject.Inject;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.web.service.TickerWebSocketServer;
import com.grahamcrockford.oco.wiring.EnvironmentInitialiser;
import com.grahamcrockford.oco.wiring.ManagedServiceTask;
import com.grahamcrockford.oco.wiring.WebResource;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.websockets.WebsocketBundle;

public class WebApplication extends Application<OcoConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebApplication.class);

  public static void main(final String[] args) throws Exception {
    new WebApplication().run(args);
  }

  @Inject private Set<Service> services;
  @Inject private Set<EnvironmentInitialiser> environmentInitialisers;
  @Inject private Set<WebResource> webResources;
  @Inject private Set<Managed> managedTasks;
  @Inject private Set<HealthCheck> healthChecks;

  private WebsocketBundle websocketBundle;


  @Override
  public String getName() {
    return "Background Trade Control: Web API";
  }

  @Override
  public void initialize(final Bootstrap<OcoConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
      new SubstitutingSourceProvider(
        bootstrap.getConfigurationSourceProvider(),
        new EnvironmentVariableSubstitutor()
      )
    );
    websocketBundle = new WebsocketBundle(new Class[] {});
    bootstrap.addBundle(websocketBundle);
  }

  @Override
  public void run(final OcoConfiguration configuration, final Environment environment) {

    // Jersey client
    final Client jerseyClient = new JerseyClientBuilder(environment)
        .using(configuration.getJerseyClientConfiguration()).build(getName());

    // Injector
    Injector injector = Guice.createInjector(
        new WebModule(configuration, environment.getObjectMapper(), jerseyClient));
    injector.injectMembers(this);

    environment.servlets().addFilter("GuiceFilter", GuiceFilter.class)
      .addMappingForUrlPatterns(java.util.EnumSet.allOf(javax.servlet.DispatcherType.class), true, "/*");

    // Any environment initialisation
    environmentInitialisers.stream()
      .peek(t -> LOGGER.info("Initialising environment for {}", t))
      .forEach(t -> t.init(environment));

    // Any managed tasks
    managedTasks.stream()
      .peek(t -> LOGGER.info("Starting managed task {}", t))
      .forEach(environment.lifecycle()::manage);

    // And any bound services
    services.stream()
      .peek(t -> LOGGER.info("Starting managed task {}", t))
      .map(ManagedServiceTask::new)
      .forEach(environment.lifecycle()::manage);

    // And any REST resources
    webResources.stream()
      .peek(t -> LOGGER.info("Registering resource {}", t))
      .forEach(environment.jersey()::register);

    // And health checks
    healthChecks.stream()
      .peek(t -> LOGGER.info("Registering resource {}", t))
      .forEach(t -> environment.healthChecks().register(t.getClass().getSimpleName(), t));

    final ServerEndpointConfig config = ServerEndpointConfig.Builder.create(TickerWebSocketServer.class, "/api/ticker-ws").build();
    config.getUserProperties().put(Injector.class.getName(), injector);
    websocketBundle.addEndpoint(config);
  }
}