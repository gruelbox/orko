package com.grahamcrockford.oco.signal;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import io.dropwizard.lifecycle.Managed;

public class SignalModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(UserTradeSignalGenerator.class);
  }
}