package com.gruelbox.orko.wiring;

/*-
 * ===============================================================================L
 * Orko Base
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

import java.util.concurrent.ExecutorService;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import io.dropwizard.lifecycle.Managed;

public class WiringModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Managed.class).addBinding()
      .to(ExecutorServiceManager.class);
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
}
