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
package com.gruelbox.orko.wiring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import java.util.concurrent.ExecutorService;

public class WiringModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(ExecutorServiceManager.class);
  }

  @Provides
  @Singleton
  EventBus eventBus() {
    return new EventBus();
  }

  @Provides
  ExecutorService executor(ExecutorServiceManager managedExecutor) {
    return managedExecutor.executor();
  }

  @Provides
  ObjectMapper objectMapper(Environment environment) {
    return environment.getObjectMapper();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof WiringModule;
  }

  @Override
  public int hashCode() {
    return WiringModule.class.getName().hashCode();
  }
}
