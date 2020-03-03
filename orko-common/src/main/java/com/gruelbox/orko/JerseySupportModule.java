/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import javax.ws.rs.client.Client;

class JerseySupportModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class)
        .addBinding()
        .toInstance(
            environment ->
                environment.jersey().register(new JerseyMappingErrorLoggingExceptionHandler()));
  }

  @Provides
  @Singleton
  Client jerseyClient(Environment environment, JerseyClientConfiguration configuration) {
    return new JerseyClientBuilder(environment)
        .using(configuration == null ? new JerseyClientConfiguration() : configuration)
        .build("client");
  }
}
