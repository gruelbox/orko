package com.gruelbox.orko;

import java.util.Set;

import javax.inject.Inject;

import org.hibernate.SessionFactory;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.gruelbox.orko.db.DatabaseSetup;
import com.gruelbox.orko.db.EntityContribution;
import com.gruelbox.orko.db.HibernateBundleFactory;
import com.gruelbox.tools.dropwizard.guice.GuiceBundle;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public abstract class BaseApplication extends Application<OrkoConfiguration> {

  @Inject private DatabaseSetup databaseSetup;
  @Inject private Set<EntityContribution> entityContributions;

  private HibernateBundle<OrkoConfiguration> hibernateBundle;

  @Override
  public void initialize(final Bootstrap<OrkoConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
      new SubstitutingSourceProvider(
        bootstrap.getConfigurationSourceProvider(),
        new EnvironmentVariableSubstitutor(false)
      )
    );

    hibernateBundle = HibernateBundleFactory.create(
      (OrkoConfiguration configuration) -> configuration.getDatabase(),
      () -> entityContributions // not ready yet
    );

    bootstrap.addBundle(
      new GuiceBundle<OrkoConfiguration>(
        this,
        new OrkoApplicationModule(),
        new Module() {
          @Override
          public void configure(Binder binder) {
            binder.bind(HibernateBundle.class).toInstance(hibernateBundle);
            binder.bind(SessionFactory.class).toProvider(() -> hibernateBundle.getSessionFactory()); // not ready yet
          }
        },
        createApplicationModule()
      )
    );

    bootstrap.addBundle(hibernateBundle);
  }

  protected abstract Module createApplicationModule();

  @Override
  public void run(final OrkoConfiguration configuration, final Environment environment) {
    databaseSetup.setup();
  }
}