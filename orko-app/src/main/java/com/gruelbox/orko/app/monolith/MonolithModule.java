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

package com.gruelbox.orko.app.monolith;

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.auth.AuthModule;
import com.gruelbox.orko.db.DbResource;
import com.gruelbox.orko.exchange.ExchangeConfiguration;
import com.gruelbox.orko.exchange.ExchangeResourceModule;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.jobrun.InProcessJobSubmitter;
import com.gruelbox.orko.jobrun.JobSubmitter;
import com.gruelbox.orko.marketdata.SimulatorModule;
import com.gruelbox.orko.websocket.WebSocketModule;
import com.gruelbox.tools.dropwizard.guice.Configured;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

/**
 * Top level bindings.
 */
class MonolithModule extends AbstractModule implements Configured<OrkoConfiguration> {

  private OrkoConfiguration configuration;

  @Override
  public void setConfiguration(OrkoConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    install(new AuthModule(configuration.getAuth()));
    install(new WebSocketModule());
    install(new ExchangeResourceModule());
    bind(JobSubmitter.class).to(InProcessJobSubmitter.class);
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class)
      .addBinding().to(MonolithEnvironment.class);
    Multibinder.newSetBinder(binder(), WebResource.class)
      .addBinding().to(DbResource.class);
    if (isSimulatorEnabled())
      install(new SimulatorModule());
  }

  private boolean isSimulatorEnabled() {
    if (configuration.getExchanges() == null)
      return false;
    ExchangeConfiguration exchangeConfiguration = configuration.getExchanges().get(Exchanges.SIMULATED);
    return exchangeConfiguration != null && exchangeConfiguration.isAuthenticated();
  }

  @Provides
  @Named(AuthModule.ROOT_PATH)
  @Singleton
  String rootPath(OrkoConfiguration configuration) {
    return configuration.getRootPath();
  }

  @Provides
  @Named(AuthModule.WEBSOCKET_ENTRY_POINT)
  @Singleton
  String webSocketEntryPoint(OrkoConfiguration configuration) {
    return WebSocketModule.ENTRY_POINT;
  }
}