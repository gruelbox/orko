package com.grahamcrockford.oco.notification;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class NotificationServiceImpl implements TransientNotificationService {

  private final EventBus eventBus;

  @Inject
  NotificationServiceImpl(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public void send(Notification notification) {
    eventBus.post(notification);
  }

  @Override
  public void error(String message, Throwable cause) {
    error(message);
  }
}