package com.grahamcrockford.oco.web;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.api.util.EnvironmentInitialiser;

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

  @Inject private Set<EnvironmentInitialiser> environmentInitialisers;
  @Inject private Set<WebResource> webResources;
  @Inject private Set<Managed> managedTasks;


  @Override
  public String getName() {
    return "Background Trade Control: Web API";
  }

  @Metered
  @Timed
  @ExceptionMetered
  @ServerEndpoint("/api/fuck")
  public static final class FuckServer {

    private final AtomicBoolean closed = new AtomicBoolean();

    @OnOpen
    public void myOnOpen(final javax.websocket.Session session) throws IOException, InterruptedException {
      try {
        session.getBasicRemote().sendText("Fucking welcome.");
        while (session.isOpen()) {
          session.getBasicRemote().sendText("Fuck.");
          Thread.sleep(1000);
        }
      } catch (Throwable e) {
        session.close();
      }
    }

    @OnMessage
    public void myOnMsg(final javax.websocket.Session session, String message) {
    }

    @OnClose
    public void myOnClose(final javax.websocket.Session session, CloseReason cr) {
    }

    @OnError
    public void onError(Throwable error) {
    }

  }

  @Override
  public void initialize(final Bootstrap<OcoConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
      new SubstitutingSourceProvider(
        bootstrap.getConfigurationSourceProvider(),
        new EnvironmentVariableSubstitutor()
      )
    );
    bootstrap.addBundle(new WebsocketBundle(null, ImmutableSet.of(FuckServer.class), Collections.emptyList()));
  }

  @Override
  public void run(final OcoConfiguration configuration, final Environment environment) {

    // Jersey client
    final Client jerseyClient = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration()).build(getName());

    // Injector
    final Injector injector = Guice.createInjector(new WebModule(configuration, environment.getObjectMapper(), jerseyClient));
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

    // And any REST resources
    webResources.stream()
      .peek(t -> LOGGER.info("Registering resource {}", t))
      .forEach(environment.jersey()::register);
  }
}