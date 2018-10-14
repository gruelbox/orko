package com.grahamcrockford.oco;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.db.DbModule;
import com.grahamcrockford.oco.job.JobsModule;
import com.grahamcrockford.oco.marketdata.MarketDataModule;
import com.grahamcrockford.oco.notification.TelegramModule;
import com.grahamcrockford.oco.signal.SignalModule;
import com.grahamcrockford.oco.strategy.StrategyModule;
import com.grahamcrockford.oco.wiring.EnvironmentInitialiser;
import com.grahamcrockford.oco.wiring.WebResource;
import com.grahamcrockford.oco.wiring.WiringModule;

import io.dropwizard.lifecycle.Managed;

class CommonModule extends AbstractModule {
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
    install(new DbModule());
    install(new MarketDataModule());
    install(new JobsModule());
    install(new TelegramModule());
    install(new SignalModule());
    install(new StrategyModule());
  }
}