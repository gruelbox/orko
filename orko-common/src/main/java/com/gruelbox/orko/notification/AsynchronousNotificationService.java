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
package com.gruelbox.orko.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.job.Alert;
import com.gruelbox.orko.job.StatusUpdateJob;
import com.gruelbox.orko.jobrun.JobSubmitter;
import com.gruelbox.orko.jobrun.spi.StatusUpdate;
import com.gruelbox.orko.jobrun.spi.StatusUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class AsynchronousNotificationService implements NotificationService, StatusUpdateService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AsynchronousNotificationService.class);

  private final JobSubmitter jobSubmitter;
  private final Transactionally transactionally;

  @Inject
  AsynchronousNotificationService(JobSubmitter jobSubmitter, Transactionally transactionally) {
    this.jobSubmitter = jobSubmitter;
    this.transactionally = transactionally;
  }

  @Override
  public void send(Notification notification) {
    transactionally
        .allowingNested()
        .run(
            () ->
                jobSubmitter.submitNewUnchecked(
                    Alert.builder().notification(notification).build()));
  }

  @Override
  public void error(String message, Throwable cause) {
    LOGGER.error("Error notification: " + message, cause);
    error(message);
  }

  @Override
  public void send(StatusUpdate statusUpdate) {
    transactionally
        .allowingNested()
        .run(
            () ->
                jobSubmitter.submitNewUnchecked(
                    StatusUpdateJob.builder().statusUpdate(statusUpdate).build()));
  }
}
