package com.grahamcrockford.oco.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.grahamcrockford.oco.OcoConfiguration;

public class CoreModule extends AbstractModule {
  @Override
  protected void configure() {
  }

  @Provides
  TelegramConfiguration telegramConfig(OcoConfiguration configuration) {
    return configuration.getTelegram();
  }
}
