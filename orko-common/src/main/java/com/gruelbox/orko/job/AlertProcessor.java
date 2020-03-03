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
package com.gruelbox.orko.job;

import static com.gruelbox.orko.jobrun.spi.Status.SUCCESS;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.notification.SynchronousNotificationService;

class AlertProcessor implements Alert.Processor {

  private final SynchronousNotificationService notificationService;
  private final Alert job;

  @AssistedInject
  public AlertProcessor(
      @Assisted Alert job,
      @Assisted JobControl jobControl,
      SynchronousNotificationService notificationService) {
    this.job = job;
    this.notificationService = notificationService;
  }

  @Override
  public void setReplacedJob(Alert job) {
    // No-op
  }

  @Override
  public Status start() {
    notificationService.send(job.notification());
    return SUCCESS;
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(
          new FactoryModuleBuilder()
              .implement(Alert.Processor.class, AlertProcessor.class)
              .build(Alert.Processor.ProcessorFactory.class));
    }
  }
}
