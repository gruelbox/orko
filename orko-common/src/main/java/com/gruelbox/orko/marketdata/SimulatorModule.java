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
package com.gruelbox.orko.marketdata;

import java.math.BigDecimal;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.simulated.SimulatedExchange;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.exchange.Exchanges;

import io.dropwizard.lifecycle.Managed;

public class SimulatorModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Service.class)
        .addBinding().to(SimulatedOrderBookActivity.class);
    Multibinder.newSetBinder(binder(), Managed.class)
        .addBinding().to(DepositCreator.class);
  }

  /**
   * Provides $200,000 to play with
   */
  private static final class DepositCreator implements Managed {

    private final ExchangeService exchangeService;

    @Inject
    DepositCreator(ExchangeService exchangeService) {
      this.exchangeService = exchangeService;
    }

    @Override
    public void start() throws Exception {
      SimulatedExchange exchange = (SimulatedExchange) exchangeService.get(Exchanges.SIMULATED);
      exchange.getAccountService().deposit(Currency.USD, new BigDecimal(200000));
    }

    @Override
    public void stop() throws Exception {
      // No-op
    }
  }
}
