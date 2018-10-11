package com.grahamcrockford.oco.notification;

import static com.grahamcrockford.oco.notification.NotificationLevel.ALERT;
import static com.grahamcrockford.oco.notification.NotificationLevel.ERROR;
import static com.grahamcrockford.oco.notification.NotificationLevel.INFO;
import com.google.inject.ImplementedBy;

/**
 * Service for notifying the user of an important event.
 */
@ImplementedBy(RetryingMessageService.class)
public interface NotificationService {

  /**
   * Sends the notification.
   *
   * @param notification
   */
  void send(Notification notification);

  /**
   * Sends a notification message asynchronously.
   *
   * @param message the message.
   */
  default void info(String message) {
    send(Notification.create(message, INFO));
  }

  /**
   * Sends a notification message asynchronously.
   *
   * @param message the message.
   */
  default void alert(String message) {
    send(Notification.create(message, ALERT));
  }

  /**
   * Sends a notification message asynchronously.
   *
   * @param message the message.
   */
  default void error(String message) {
    send(Notification.create(message, ERROR));
  }

  /**
   * Sends a notification message asynchronously.
   *
   * @param message the message.
   * @param cause the cause
   */
  void error(String message, Throwable cause);

}