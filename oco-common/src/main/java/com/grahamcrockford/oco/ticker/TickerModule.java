package com.grahamcrockford.oco.ticker;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class TickerModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ExchangeEventRegistry.class).to(ExchangeEventBus.class);
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(TickerGenerator.class);
  }
}