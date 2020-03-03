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

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.gruelbox.orko.docker.DockerSecretSubstitutor;
import com.gruelbox.tools.dropwizard.guice.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.concurrent.ExecutorService;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseApplication<T extends Configuration & BaseApplicationConfiguration>
    extends Application<T> implements Module {

  private DockerSecretSubstitutor dockerSecretSubstitutor;

  @Inject private ExecutorService executorService;

  private ServerProvider serverProvider = new ServerProvider();

  @Override
  public void initialize(final Bootstrap<T> bootstrap) {

    dockerSecretSubstitutor = new DockerSecretSubstitutor(false, false, true);
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)),
            dockerSecretSubstitutor));

    bootstrap.addBundle(
        new GuiceBundle<T>(this, this, new ChildProcessSupportModule(), createApplicationModule()));
  }

  protected abstract Module createApplicationModule();

  @Override
  public void configure(Binder binder) {
    binder.install(new JerseySupportModule());
    binder.bind(Server.class).toProvider(serverProvider);
  }

  @Override
  public void run(T configuration, Environment environment) {
    environment.lifecycle().addServerLifecycleListener(serverProvider);

    // We can't use the logger during the docker YAML parsing, so it's delayed until here.
    Logger dockerLogger = LoggerFactory.getLogger(DockerSecretSubstitutor.class);
    if (dockerLogger.isDebugEnabled()) {
      dockerSecretSubstitutor.getLog().stream().forEach(dockerLogger::debug);
    }
  }
}
