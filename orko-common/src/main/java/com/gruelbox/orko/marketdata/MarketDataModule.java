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

import org.knowm.xchange.simulated.AccountFactory;
import org.knowm.xchange.simulated.MatchingEngineFactory;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class MarketDataModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ExchangeEventRegistry.class).to(ExchangeEventBus.class);
    Multibinder.newSetBinder(binder(), Service.class)
        .addBinding().to(MarketDataSubscriptionManager.class);
  }

  @Provides
  @Singleton
  AccountFactory accountFactory() {
    return new AccountFactory();
  }

  @Provides
  @Singleton
  MatchingEngineFactory matchingEngineFactory(AccountFactory accountFactory) {
    return new MatchingEngineFactory(accountFactory);
  }
}