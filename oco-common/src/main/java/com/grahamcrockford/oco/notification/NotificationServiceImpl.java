package com.grahamcrockford.oco.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.inject.Inject;
import com.grahamcrockford.oco.notification.NotificationEvent.NotificationType;

class NotificationServiceImpl implements NotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

  private final AsyncEventBus eventBus;

  @Inject
  NotificationServiceImpl(AsyncEventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public void info(String message) {
    LOGGER.info("Notification: " + message);
    eventBus.post(NotificationEvent.create(message, NotificationType.INFO));
  }

  @Override
  public void error(String message) {
    LOGGER.error("Error notification: " + message);
    eventBus.post(NotificationEvent.create(message, NotificationType.ERROR));
  }

  @Override
  public void error(String message, Throwable cause) {
    LOGGER.error("Error notification: " + message, cause);
    eventBus.post(NotificationEvent.create(message, NotificationType.ERROR));
  }
}