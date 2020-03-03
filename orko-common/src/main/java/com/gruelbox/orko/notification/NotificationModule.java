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

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.jobrun.spi.StatusUpdateService;
import io.dropwizard.lifecycle.Managed;

public class NotificationModule extends AbstractModule {

  private final SubmissionType submissionType;

  public NotificationModule(SubmissionType submissionType) {
    this.submissionType = submissionType;
  }

  @Override
  protected void configure() {
    if (submissionType == SubmissionType.ASYNC) {
      bind(NotificationService.class).to(AsynchronousNotificationService.class);
    } else {
      bind(NotificationService.class).to(SynchronousNotificationService.class);
    }
    bind(StatusUpdateService.class).to(SynchronousStatusUpdateService.class);
    Multibinder.newSetBinder(binder(), Managed.class)
        .addBinding()
        .to(TelegramNotificationsTask.class);
    Multibinder.newSetBinder(binder(), Managed.class)
        .addBinding()
        .to(AppriseNotificationsTask.class);
  }

  public enum SubmissionType {
    ASYNC,
    SYNC
  }
}
