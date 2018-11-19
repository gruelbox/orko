package com.grahamcrockford.orko;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.orko.db.DbModule;
import com.grahamcrockford.orko.job.JobsModule;
import com.grahamcrockford.orko.marketdata.MarketDataModule;
import com.grahamcrockford.orko.notification.TelegramModule;
import com.grahamcrockford.orko.signal.SignalModule;
import com.grahamcrockford.orko.strategy.StrategyModule;
import com.grahamcrockford.orko.submit.SubmitModule;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;
import com.grahamcrockford.orko.wiring.WebResource;
import com.grahamcrockford.orko.wiring.WiringModule;

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
    install(new SubmitModule());
    install(new MarketDataModule());
    install(new JobsModule());
    install(new TelegramModule());
    install(new SignalModule());
    install(new StrategyModule());
  }
}