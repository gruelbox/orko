/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.jobrun.spi;

/** Service for notifying the user of an important event. Asynchronous. */
public interface StatusUpdateService {

  /**
   * Sends the status update..
   *
   * @param statusUpdate The status update.
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
