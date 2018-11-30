package com.gruelbox.orko.strategy;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import io.dropwizard.lifecycle.Managed;

public class StrategyModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(UserTradeNotifier.class);
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(BinanceExecutionReportLogger.class);
  }
}