package com.grahamcrockford.orko.guardian;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import io.dropwizard.lifecycle.Managed;

public class GuardianModule extends AbstractModule {

  private final boolean mq;

  public GuardianModule(boolean mq) {
    this.mq = mq;
  }

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(GuardianLoop.class);
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(MonitorExchangeSocketHealth.class);
    if (mq) {
      Multibinder.newSetBinder(binder(), Service.class).addBinding().to(MqListener.class);
    }
  }
}