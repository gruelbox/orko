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

package com.gruelbox.orko.app.exchangebroker;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.exchange.ExchangeConfiguration;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.marketdata.MarketDataModule;
import com.gruelbox.orko.marketdata.MarketDataModule.RemoteType;
import com.gruelbox.orko.marketdata.SimulatorModule;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.notification.TransientNotificationService;
import com.gruelbox.tools.dropwizard.guice.Configured;

/**
 * Top level bindings.
 */
class ExchangeBrokerModule extends AbstractModule implements Configured<OrkoConfiguration> {

  private OrkoConfiguration configuration;

  @Override
  public void setConfiguration(OrkoConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    install(new MarketDataModule(RemoteType.LOCAL));
    bind(NotificationService.class)
        .to(TransientNotificationService.class);
    Multibinder.newSetBinder(binder(), Service.class)
        .addBinding().to(ExchangeBrokerManager.class);
    if (isSimulatorEnabled())
      install(new SimulatorModule());
  }

  private boolean isSimulatorEnabled() {
    if (configuration.getExchanges() == null)
      return false;
    ExchangeConfiguration exchangeConfiguration = configuration.getExchanges().get(Exchanges.SIMULATED);
    return exchangeConfiguration != null && exchangeConfiguration.isAuthenticated();
  }
}