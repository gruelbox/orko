/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gruelbox.orko;

import javax.inject.Inject;

import com.google.inject.Module;
import com.gruelbox.orko.db.DatabaseSetup;
import com.gruelbox.orko.docker.DockerSecretSubstitutor;
import com.gruelbox.tools.dropwizard.guice.GuiceBundle;
import com.gruelbox.tools.dropwizard.guice.hibernate.GuiceHibernateModule;
import com.gruelbox.tools.dropwizard.guice.hibernate.HibernateBundleFactory;

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
        new SubstitutingSourceProvider(
          bootstrap.getConfigurationSourceProvider(),
          new EnvironmentVariableSubstitutor(false)
        ),
        new DockerSecretSubstitutor(false, false, true)
      )
    );

    HibernateBundleFactory<OrkoConfiguration> hibernateBundleFactory
      = new HibernateBundleFactory<>(configuration -> configuration.getDatabase().toDataSourceFactory());

    bootstrap.addBundle(
      new GuiceBundle<OrkoConfiguration>(
        this,
        new OrkoApplicationModule(),
        new GuiceHibernateModule(hibernateBundleFactory),
        createApplicationModule()
      )
    );

    bootstrap.addBundle(hibernateBundleFactory.bundle());
  }

  protected abstract Module createApplicationModule();

  @Override
  public void run(final OrkoConfiguration configuration, final Environment environment) {
    databaseSetup.setup();
  }
}