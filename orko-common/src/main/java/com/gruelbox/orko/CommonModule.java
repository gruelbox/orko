package com.gruelbox.orko;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.db.DbModule;
import com.gruelbox.orko.job.JobsModule;
import com.gruelbox.orko.jobrun.JobRunModule;
import com.gruelbox.orko.jobrun.spi.JobRunConfiguration;
import com.gruelbox.orko.marketdata.MarketDataModule;
import com.gruelbox.orko.notification.NotificationModule;
import com.gruelbox.orko.signal.SignalModule;
import com.gruelbox.orko.strategy.StrategyModule;
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
    install(new MarketDataModule());
    install(new JobsModule());
    install(new NotificationModule());
    install(new SignalModule());
    install(new StrategyModule());
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