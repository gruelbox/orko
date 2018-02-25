package com.grahamcrockford.oco.telegram;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.telegram.TelegramConfiguration;

public class TelegramModule extends AbstractModule {
  @Override
  protected void configure() {
  }

  @Provides
  TelegramConfiguration telegramConfig(OcoConfiguration configuration) {
    return configuration.getTelegram();
  }
}
