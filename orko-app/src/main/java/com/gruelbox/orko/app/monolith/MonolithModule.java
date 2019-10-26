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
import com.gruelbox.orko.wiring.AbstractConfiguredModule;
import com.gruelbox.tools.dropwizard.guice.hibernate.GuiceHibernateModule;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

/**
 * Top level bindings.
 */
class MonolithModule extends AbstractConfiguredModule<OrkoConfiguration> {

  private final GuiceHibernateModule guiceHibernateModule;

  MonolithModule(GuiceHibernateModule guiceHibernateModule) {
    super();
    this.guiceHibernateModule = guiceHibernateModule;
  }

  @Override
  protected void configure() {
    install(guiceHibernateModule);
    install(new AuthModule());
    install(new WebSocketModule());
    install(new ExchangeResourceModule());
    bind(JobSubmitter.class).to(InProcessJobSubmitter.class);
    Multibinder.newSetBinder(binder(), WebResource.class)
      .addBinding().to(DbResource.class);
    if (isSimulatorEnabled())
      install(new SimulatorModule());
  }

  private boolean isSimulatorEnabled() {
    if (getConfiguration().getExchanges() == null)
      return false;
    ExchangeConfiguration exchangeConfiguration = getConfiguration().getExchanges().get(Exchanges.SIMULATED);
    return exchangeConfiguration != null && exchangeConfiguration.isAuthenticated();
  }

  @Provides
  @Named(AuthModule.ROOT_PATH)
  @Singleton
  String rootPath() {
    return getConfiguration().getRootPath();
  }

  @Provides
  @Named(AuthModule.WEBSOCKET_ENTRY_POINT)
  @Singleton
  String webSocketEntryPoint() {
    return WebSocketModule.ENTRY_POINT;
  }
}