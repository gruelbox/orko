package com.grahamcrockford.oco.guardian;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class GuardianModule extends AbstractModule {

  private final boolean mq;

  public GuardianModule(boolean mq) {
    this.mq = mq;
  }

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(GuardianLoop.class);
    if (mq) {
      Multibinder.newSetBinder(binder(), Service.class).addBinding().to(MqListener.class);
    }
  }
}