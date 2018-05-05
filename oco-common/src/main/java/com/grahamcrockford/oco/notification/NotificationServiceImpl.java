package com.grahamcrockford.oco.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

class NotificationServiceImpl implements NotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

  private final EventBus eventBus;

  @Inject
  NotificationServiceImpl(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public void safeSendMessage(String message) {
    try {
      sendMessage(message);
    } catch (Throwable t) {
      LOGGER.error(this + " failed to send Telegram message", t);
    }
  }

  @Override
  public void sendMessage(String message) {
    eventBus.post(NotificationEvent.create(message));
  }
}