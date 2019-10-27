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
import com.gruelbox.orko.auth.AuthModule;
import com.gruelbox.orko.db.DbResource;
import com.gruelbox.orko.exchange.ExchangeConfiguration;
import com.gruelbox.orko.exchange.ExchangeModule;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.job.JobsModule;
import com.gruelbox.orko.jobrun.InProcessJobSubmitter;
import com.gruelbox.orko.jobrun.JobRunModule;
import com.gruelbox.orko.jobrun.JobSubmitter;
import com.gruelbox.orko.marketdata.MarketDataModule;
import com.gruelbox.orko.marketdata.SimulatorModule;
import com.gruelbox.orko.notification.NotificationModule;
import com.gruelbox.orko.strategy.StrategyModule;
import com.gruelbox.orko.subscription.SubscriptionModule;
import com.gruelbox.orko.support.SupportModule;
import com.gruelbox.orko.websocket.WebSocketModule;
import com.gruelbox.tools.dropwizard.guice.Configured;
import com.gruelbox.tools.dropwizard.guice.hibernate.GuiceHibernateModule;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

/**
 * Top level bindings.
 */
class MonolithModule extends AbstractModule implements Configured<MonolithConfiguration> {

  private final GuiceHibernateModule guiceHibernateModule;
  private MonolithConfiguration configuration;

  MonolithModule(GuiceHibernateModule guiceHibernateModule) {
    super();
    this.guiceHibernateModule = guiceHibernateModule;
  }

  @Override
  public void setConfiguration(MonolithConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    install(guiceHibernateModule);
    configuration.bind(binder());

    install(new AuthModule(configuration.getAuth()));
    install(new WebSocketModule());
    install(new ExchangeModule());
    install(new JobRunModule());
    install(new MarketDataModule());
    install(new SubscriptionModule());
    install(new JobsModule());
    install(new NotificationModule());
    install(new StrategyModule());
    install(new SupportModule());

    bind(JobSubmitter.class).to(InProcessJobSubmitter.class);
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(DbResource.class);
    if (isSimulatorEnabled()) {
      install(new SimulatorModule());
    }
  }

  private boolean isSimulatorEnabled() {
    if (configuration.getExchanges() == null)
      return false;
    ExchangeConfiguration exchangeConfiguration = configuration.getExchanges().get(Exchanges.SIMULATED);
    return exchangeConfiguration != null && exchangeConfiguration.isAuthenticated();
  }

  @Provides
  @Named(AuthModule.BIND_ROOT_PATH)
  @Singleton
  String rootPath() {
    return configuration.getRootPath();
  }
}