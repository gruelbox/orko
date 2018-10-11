package com.grahamcrockford.oco.notification;

import com.google.inject.ImplementedBy;

/**
 * Service for notifying the user of an important event. Asynchronous.
 */
@ImplementedBy(RetryingMessageService.class)
public interface StatusUpdateService {

  /**
   * Sends the status update..
   *
   * @param statusUpdate
   */
  void send(StatusUpdate statusUpdate);

  /**
   * Sends a message indicating the status of an asynchronous request.
   *
   * @param requestId The id of the asynchronous request.
   * @param notificationStatus The current request status.
   */
  default void status(String requestId, NotificationStatus notificationStatus) {
    send(StatusUpdate.create(requestId, notificationStatus, null));
  }

  /**
   * Sends a message indicating the status of an asynchronous request.
   *
   * @param requestId The id of the asynchronous request.
   * @param notificationStatus The current request status.
   * @param payload The object processed.
   */
  default void status(String requestId, NotificationStatus notificationStatus, Object payload) {
    send(StatusUpdate.create(requestId, notificationStatus, payload));
  }
}