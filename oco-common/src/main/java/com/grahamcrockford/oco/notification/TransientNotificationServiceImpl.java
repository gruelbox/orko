package com.grahamcrockford.oco.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.grahamcrockford.oco.notification.NotificationEvent.NotificationType;

class TransientNotificationServiceImpl implements TransientNotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransientNotificationServiceImpl.class);

  private final EventBus eventBus;

  @Inject
  TransientNotificationServiceImpl(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public void info(String message) {
    eventBus.post(NotificationEvent.create(message, NotificationType.INFO));
  }

  @Override
  public void error(String message) {
    eventBus.post(NotificationEvent.create(message, NotificationType.ERROR));
  }

  @Override
  public void error(String message, Throwable cause) {
    eventBus.post(NotificationEvent.create(message, NotificationType.ERROR));
  }
}