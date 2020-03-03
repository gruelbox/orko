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
package com.gruelbox.orko.app.monolith;

import static com.gruelbox.orko.exchange.MarketDataModule.MarketDataSource.MANAGE_LOCALLY;
import static com.gruelbox.orko.exchange.MarketDataModule.MarketDataSource.MANAGE_REMOTELY;
import static com.gruelbox.orko.notification.NotificationModule.SubmissionType.ASYNC;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.auth.AuthModule;
import com.gruelbox.orko.db.DbResource;
import com.gruelbox.orko.exchange.ExchangeConfiguration;
import com.gruelbox.orko.exchange.ExchangeResource;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.exchange.MarketDataModule;
import com.gruelbox.orko.exchange.SimulatedExchangeActivityModule;
import com.gruelbox.orko.job.StandardJobLibraryModule;
import com.gruelbox.orko.jobrun.InProcessJobSubmitter;
import com.gruelbox.orko.jobrun.JobRunModule;
import com.gruelbox.orko.jobrun.JobSubmitter;
import com.gruelbox.orko.monitor.MonitorModule;
import com.gruelbox.orko.notification.NotificationModule;
import com.gruelbox.orko.subscription.SubscriptionModule;
import com.gruelbox.orko.support.SupportModule;
import com.gruelbox.orko.websocket.WebSocketModule;
import com.gruelbox.tools.dropwizard.guice.Configured;
import com.gruelbox.tools.dropwizard.guice.hibernate.GuiceHibernateModule;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Top level bindings. */
class MonolithModule extends AbstractModule implements Configured<MonolithConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonolithModule.class);

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

    // Enable Hibernate access
    install(guiceHibernateModule);

    // Make elements of configuration available to child modules
    configuration.bind(binder());

    // Publicly visible and requires authentication
    install(new AuthModule(configuration.getAuth()));

    // Exposes the web socket
    install(new WebSocketModule());

    // Both managing and running jobs (TODO the two are currently coupled)
    install(new JobRunModule());
    bind(JobSubmitter.class).to(InProcessJobSubmitter.class);
    install(new StandardJobLibraryModule());

    // Manages UI support
    install(new SubscriptionModule());
    install(new SupportModule());

    // Forwards notifications to Telegram asynchronously
    install(new NotificationModule(ASYNC));

    LOGGER.info(
        "Market data source config:\n  ws={}\n  api={}",
        configuration.getRemoteMarketData().getWebSocketUri(),
        configuration.getRemoteMarketData().getExchangeEndpointUri());

    if (configuration.getRemoteMarketData().isEnabled()) {
      // Remote market management
      LOGGER.info("Using remote data source");
      install(new MarketDataModule(MANAGE_REMOTELY));
    } else {
      LOGGER.info("Using local data source");
      // Both managing and running market data access
      install(new MarketDataModule(MANAGE_LOCALLY));
      // Monitors various status issues are fires notifications if things go wrong.
      install(new MonitorModule());
      // Runs some simulated order book activity on the simulated exchange
      if (isSimulatorEnabled()) {
        LOGGER.info("Enabling simulator");
        install(new SimulatedExchangeActivityModule());
      }
    }

    // Exposes API access to exchanges
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(ExchangeResource.class);

    // Provides access to the database
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(DbResource.class);
  }

  private boolean isSimulatorEnabled() {
    if (configuration.getExchanges() == null) return false;
    ExchangeConfiguration exchangeConfiguration =
        configuration.getExchanges().get(Exchanges.SIMULATED);
    return exchangeConfiguration != null && exchangeConfiguration.isAuthenticated();
  }

  @Provides
  @Named(AuthModule.BIND_ROOT_PATH)
  @Singleton
  String rootPath() {
    return configuration.getRootPath();
  }
}
