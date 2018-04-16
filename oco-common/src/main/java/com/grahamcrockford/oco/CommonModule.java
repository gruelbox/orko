package com.grahamcrockford.oco;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.db.DbModule;
import com.grahamcrockford.oco.mq.MqModule;
import com.grahamcrockford.oco.telegram.TelegramModule;
import com.grahamcrockford.oco.ticker.TickerModule;
import com.grahamcrockford.oco.wiring.EnvironmentInitialiser;
import com.grahamcrockford.oco.wiring.WiringModule;

import io.dropwizard.lifecycle.Managed;

public class CommonModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Service.class);
    Multibinder.newSetBinder(binder(), Managed.class);
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class);
    Multibinder.newSetBinder(binder(), HealthCheck.class);
    install(new WiringModule());
    install(new DbModule());
    install(new MqModule());
    install(new TickerModule());
    install(new TelegramModule());
  }
}