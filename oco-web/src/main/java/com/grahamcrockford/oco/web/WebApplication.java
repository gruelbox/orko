package com.grahamcrockford.oco.web;

import java.util.Set;

import javax.inject.Inject;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.auth.AuthenticatedEndpointConfigurator;
import com.grahamcrockford.oco.wiring.EnvironmentInitialiser;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.websockets.WebsocketBundle;

public class WebApplication extends Application<OcoConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebApplication.class);

  public static void main(final String[] args) throws Exception {
    new WebApplication().run(args);
  }

  @Inject private Set<EnvironmentInitialiser> environmentInitialisers;
  @Inject private AuthenticatedEndpointConfigurator authenticatedEndpointConfigurator;

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

    final ServerEndpointConfig config = ServerEndpointConfig.Builder
        .create(OcoWebSocketServer.class, "/api/ws")
//        .configurator(authenticatedEndpointConfigurator)
        .build();
    config.getUserProperties().put(Injector.class.getName(), injector);
    websocketBundle.addEndpoint(config);
  }
}