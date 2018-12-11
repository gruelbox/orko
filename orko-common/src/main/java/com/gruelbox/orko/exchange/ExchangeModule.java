package com.gruelbox.orko.exchange;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import io.dropwizard.lifecycle.Managed;

public class ExchangeModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Managed.class)
      .addBinding().to(MonitorExchangeSocketHealth.class);
  }
}