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
package com.gruelbox.orko.exchange;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.wiring.WiringModule;
import io.dropwizard.lifecycle.Managed;
import org.knowm.xchange.simulated.AccountFactory;
import org.knowm.xchange.simulated.MatchingEngineFactory;

public class MarketDataModule extends AbstractModule {

  private final MarketDataSource marketDataSource;

  public MarketDataModule(MarketDataSource marketDataSource) {
    this.marketDataSource = marketDataSource;
  }

  @Override
  protected void configure() {
    install(new WiringModule());
    bind(ExchangeEventRegistry.class).to(ExchangeEventBus.class);
    bind(MarketDataSubscriptionManager.class).to(SubscriptionPublisher.class);
    switch (marketDataSource) {
      case MANAGE_LOCALLY:
        bind(SubscriptionController.class).to(SubscriptionControllerImpl.class);
        Multibinder.newSetBinder(binder(), Service.class)
            .addBinding()
            .to(SubscriptionControllerImpl.class);
        Multibinder.newSetBinder(binder(), HealthCheck.class)
            .addBinding()
            .to(ExchangeAccessHealthCheck.class);
        break;
      case MANAGE_REMOTELY:
        bind(SubscriptionController.class).to(SubscriptionControllerRemoteImpl.class);
        Multibinder.newSetBinder(binder(), Managed.class)
            .addBinding()
            .to(SubscriptionControllerRemoteImpl.class);
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + marketDataSource);
    }
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

  public enum MarketDataSource {
    MANAGE_LOCALLY,
    MANAGE_REMOTELY
  }
}
