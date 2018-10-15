package com.grahamcrockford.orko.notification;

import static com.grahamcrockford.orko.notification.NotificationLevel.ALERT;
import static com.grahamcrockford.orko.notification.NotificationLevel.ERROR;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.dropwizard.lifecycle.Managed;

@Singleton
final class TelegramNotificationsTask implements Managed {

  private static final Set<NotificationLevel> SEND_FOR = ImmutableSet.of(ERROR, ALERT);

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
  void notify(Notification notification) {
    if (SEND_FOR.contains(notification.level())) {
      telegramService.sendMessage(notification.message());
    }
  }
}