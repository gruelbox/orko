package com.grahamcrockford.oco.notification;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;

@Singleton
final class TelegramNotificationsTask implements Managed {

  private final TelegramService telegramService;
  private final EventBus eventBus;

  @Inject
  TelegramNotificationsTask(TelegramService telegramService, EventBus eventBus) {
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