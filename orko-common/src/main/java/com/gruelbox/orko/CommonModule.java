package com.gruelbox.orko;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.db.DbConfiguration;
import com.gruelbox.orko.db.DbModule;
import com.gruelbox.orko.job.JobsModule;
import com.gruelbox.orko.marketdata.MarketDataModule;
import com.gruelbox.orko.notification.TelegramModule;
import com.gruelbox.orko.signal.SignalModule;
import com.gruelbox.orko.strategy.StrategyModule;
import com.gruelbox.orko.submit.SubmitModule;
import com.gruelbox.orko.wiring.EnvironmentInitialiser;
import com.gruelbox.orko.wiring.WebResource;
import com.gruelbox.orko.wiring.WiringModule;

import io.dropwizard.lifecycle.Managed;

class CommonModule extends AbstractModule {

  private final DbConfiguration dbConfiguration;

  CommonModule(DbConfiguration dbConfiguration) {
    this.dbConfiguration = dbConfiguration;
  }

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Service.class);
    Multibinder.newSetBinder(binder(), Managed.class);
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class)
      .addBinding()
      .toInstance(environment -> {
        environment.jersey().register(new JerseyMappingErrorLoggingExceptionHandler());
      });
    Multibinder.newSetBinder(binder(), HealthCheck.class);
    Multibinder.newSetBinder(binder(), WebResource.class);
    install(new WiringModule());
    install(new DbModule(dbConfiguration));
    install(new SubmitModule());
    install(new MarketDataModule());
    install(new JobsModule());
    install(new TelegramModule());
    install(new SignalModule());
    install(new StrategyModule());
  }
}