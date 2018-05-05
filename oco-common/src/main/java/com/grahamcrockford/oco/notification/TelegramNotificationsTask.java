package com.grahamcrockford.oco.notification;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;

@Singleton
final class TelegramNotificationsTask implements Managed {

  private final TelegramService telegramService;
  private final AsyncEventBus eventBus;

  @Inject
  TelegramNotificationsTask(TelegramService telegramService, AsyncEventBus eventBus) {
    this.telegramService = telegramService;
    this.eventBus = eventBus;
  }

  @Override
  public void start() throws Exception {
    eventBus.register(this);
  }

  @Override
  public void stop() throws Exception {
    eventBus.unregister(this);
  }

  @Subscribe
  void notify(NotificationEvent event) {
    telegramService.sendMessage(event.message());
  }
}