package com.grahamcrockford.oco.notification;

import com.google.inject.ImplementedBy;

/**
 * Service for notifying the user of an important event. Asynchronous.
 */
@ImplementedBy(RetryingNotificationService.class)
public interface NotificationService {

  /**
   * Sends a notification message asynchronously.
   *
   * @param message the message.
   */
  void info(String message);

  /**
   * Sends a notification message asynchronously.
   *
   * @param message the message.
   */
  void error(String message);

  /**
   * Sends a notification message asynchronously.
   *
   * @param message the message.
   * @param cause the cause
   */
  void error(String message, Throwable cause);

}