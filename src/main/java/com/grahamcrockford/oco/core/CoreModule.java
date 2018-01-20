package com.grahamcrockford.oco.core;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.OcoConfiguration;

public class CoreModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), AbstractScheduledService.class).addBinding().to(GameLoop.class);
  }

  @Provides
  TelegramConfiguration telegramConfig(OcoConfiguration configuration) {
    return configuration.getTelegram();
  }
}
