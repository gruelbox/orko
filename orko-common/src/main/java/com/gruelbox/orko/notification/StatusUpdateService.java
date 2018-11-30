package com.gruelbox.orko.notification;

import com.google.inject.ImplementedBy;

/**
 * Service for notifying the user of an important event. Asynchronous.
 */
@ImplementedBy(StatusUpdateServiceImpl.class)
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
   * @param status The current request status.
   */
  default void status(String requestId, Status status) {
    send(StatusUpdate.create(requestId, status, null));
  }

  /**
   * Sends a message indicating the status of an asynchronous request.
   *
   * @param requestId The id of the asynchronous request.
   * @param status The current request status.
   * @param payload The object processed.
   */
  default void status(String requestId, Status status, Object payload) {
    send(StatusUpdate.create(requestId, status, payload));
  }
}