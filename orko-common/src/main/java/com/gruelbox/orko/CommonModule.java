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

package com.gruelbox.orko;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.db.DbModule;
import com.gruelbox.orko.exchange.ExchangeModule;
import com.gruelbox.orko.job.JobsModule;
import com.gruelbox.orko.jobrun.JobRunModule;
import com.gruelbox.orko.jobrun.spi.JobRunConfiguration;
import com.gruelbox.orko.marketdata.MarketDataModule;
import com.gruelbox.orko.notification.NotificationModule;
import com.gruelbox.orko.strategy.StrategyModule;
import com.gruelbox.orko.subscription.SubscriptionModule;
import com.gruelbox.orko.support.SupportModule;
import com.gruelbox.orko.wiring.WiringModule;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;

class CommonModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class)
      .addBinding()
      .toInstance(environment -> {
        environment.jersey().register(new JerseyMappingErrorLoggingExceptionHandler());
      });
    install(new WiringModule());
    install(new DbModule());
    install(new JobRunModule());
    install(new ExchangeModule());
    install(new MarketDataModule());
    install(new SubscriptionModule());
    install(new JobsModule());
    install(new NotificationModule());
    install(new StrategyModule());
    install(new SupportModule());
  }

  @Provides
  @Singleton
  JobRunConfiguration jobRunConfiguration(OrkoConfiguration orkoConfiguration) {
    JobRunConfiguration jobRunConfiguration = new JobRunConfiguration();
    jobRunConfiguration.setDatabaseLockSeconds(orkoConfiguration.getDatabase().getLockSeconds());
    jobRunConfiguration.setGuardianLoopSeconds(orkoConfiguration.getLoopSeconds());
    return jobRunConfiguration;
  }
}