package com.grahamcrockford.oco.core;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class CoreModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(GuardianLoop.class);
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(MqListener.class);
  }
}