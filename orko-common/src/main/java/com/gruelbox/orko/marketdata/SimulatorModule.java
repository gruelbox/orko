package com.gruelbox.orko.marketdata;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class SimulatorModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Service.class)
        .addBinding().to(SimulatedOrderBookActivity.class);
  }

}
