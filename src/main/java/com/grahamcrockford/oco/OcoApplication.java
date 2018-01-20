package com.grahamcrockford.oco;

import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class OcoApplication extends Application<OcoConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(OcoApplication.class);

  public static void main(final String[] args) throws Exception {
    new OcoApplication().run(args);
  }

  @Inject
  private Set<AbstractScheduledService> services;

  @Inject
  private Set<WebResource> webResources;

  @Override
  public String getName() {
    return "oco";
  }

  @Override
  public void initialize(final Bootstrap<OcoConfiguration> bootstrap) {
    // TODO: application initialization
  }

  @Override
  public void run(final OcoConfiguration configuration, final Environment environment) {
    final Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration()).build(getName());
    final Injector injector = Guice.createInjector(new OcoModule(configuration, environment.getObjectMapper(), client));
    injector.injectMembers(this);

    services.stream()
      .peek(t -> LOGGER.info("Starting managed task {}", t))
      .map(ManagedPeriodicTask::new)
      .forEach(environment.lifecycle()::manage);
    webResources.stream()
      .peek(t -> LOGGER.info("Registering resource {}", t))
      .forEach(environment.jersey()::register);
  }

  /**
   * Allows an {@link AbstractScheduledService} to be managed by DropWizard lifecycle.
   */
  private static final class ManagedPeriodicTask implements Managed {

    private final AbstractScheduledService periodicTask;

    public ManagedPeriodicTask(AbstractScheduledService periodicTask) {
      this.periodicTask = periodicTask;
    }

    @Override
    public void start() throws Exception {
      periodicTask.startAsync().awaitRunning();
    }

    @Override
    public void stop() throws Exception {
      periodicTask.stopAsync().awaitTerminated();
    }
  }
}