package com.gruelbox.orko;

import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.gruelbox.orko.db.DatabaseSetup;
import com.gruelbox.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public abstract class BaseApplication extends Application<OrkoConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseApplication.class);

  @Inject private Set<EnvironmentInitialiser> environmentInitialisers;

  @Override
  public void initialize(final Bootstrap<OrkoConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
      new SubstitutingSourceProvider(
        bootstrap.getConfigurationSourceProvider(),
        new EnvironmentVariableSubstitutor(false)
      )
    );
  }

  protected abstract Module createApplicationModule(final OrkoConfiguration configuration, final Environment environment, Client jerseyClient);


  @Override
  public void run(final OrkoConfiguration configuration, final Environment environment) {

    // Jersey client
    final Client jerseyClient = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration()).build(getName());

    // Injector
    final Injector injector = Guice.createInjector(
      new OrkoApplicationModule(configuration, environment.getObjectMapper(), jerseyClient, environment),
      createApplicationModule(configuration, environment, jerseyClient)
    );
    injector.injectMembers(this);
    injector.getInstance(DatabaseSetup.class).setup();

    GuiceFilter guiceFilter = injector.getInstance(GuiceFilter.class);
    environment.servlets().addFilter("GuiceFilter", guiceFilter).addMappingForUrlPatterns(null, false, "/*");
    environment.admin().addFilter("GuiceFilter", guiceFilter).addMappingForUrlPatterns(null, false, "/*");

    // Any environment initialisation
    environmentInitialisers.stream()
      .peek(t -> LOGGER.info("Initialising environment for {}", t))
      .forEach(t -> t.init(environment));
  }
}