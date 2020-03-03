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
package com.gruelbox.orko.app.marketdata;

import static com.gruelbox.orko.exchange.MarketDataModule.MarketDataSource.MANAGE_LOCALLY;
import static com.gruelbox.orko.notification.NotificationModule.SubmissionType.SYNC;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.exchange.ExchangeConfiguration;
import com.gruelbox.orko.exchange.ExchangeResource;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.exchange.MarketDataModule;
import com.gruelbox.orko.exchange.SimulatedExchangeActivityModule;
import com.gruelbox.orko.monitor.MonitorModule;
import com.gruelbox.orko.notification.NotificationModule;
import com.gruelbox.orko.websocket.WebSocketModule;
import com.gruelbox.tools.dropwizard.guice.Configured;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

/** Top level bindings. */
class MarketDataAppModule extends AbstractModule implements Configured<MarketDataAppConfiguration> {

  private MarketDataAppConfiguration configuration;

  @Override
  public void setConfiguration(MarketDataAppConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {

    // Make elements of configuration available to child modules
    configuration.bind(binder());

    // Exposes the web socket
    install(new WebSocketModule());

    // Both managing and running market data access
    install(new MarketDataModule(MANAGE_LOCALLY));

    // Forwards notifications to Telegram asynchronously
    install(new NotificationModule(SYNC));

    // Monitors various status issues are fires notifications if things go wrong.
    install(new MonitorModule());

    // Exposes API access to exchanges
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(ExchangeResource.class);

    // Runs some simulated order book activity on the simulated exchange
    if (isSimulatorEnabled()) {
      install(new SimulatedExchangeActivityModule());
    }
  }

  private boolean isSimulatorEnabled() {
    if (configuration.getExchanges() == null) return false;
    ExchangeConfiguration exchangeConfiguration =
        configuration.getExchanges().get(Exchanges.SIMULATED);
    return exchangeConfiguration != null && exchangeConfiguration.isAuthenticated();
  }
}
