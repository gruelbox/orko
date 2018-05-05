package com.grahamcrockford.oco.notification;

import com.google.inject.ImplementedBy;

/**
 * Service for notifying the user of an important event.
 */
@ImplementedBy(NotificationServiceImpl.class)
public interface NotificationService {

  /**
   * Sends a notification message.
   *
   * @param message the message.
   */
  void sendMessage(String message);

  /**
   * Sends the message without throwing if there are issues with dispatch.
   *
   * @param message The message
   */
  void safeSendMessage(String message);

}