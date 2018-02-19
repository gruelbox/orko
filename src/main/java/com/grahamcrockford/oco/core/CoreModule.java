package com.grahamcrockford.oco.core;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.OcoConfiguration;

public class CoreModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(JobExecutor.Factory.class));
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(JobKeepAlive.class);
  }

  @Provides
  TelegramConfiguration telegramConfig(OcoConfiguration configuration) {
    return configuration.getTelegram();
  }
}
