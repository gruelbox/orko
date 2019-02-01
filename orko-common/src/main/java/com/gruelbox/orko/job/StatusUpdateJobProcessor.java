/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gruelbox.orko.job;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.notification.TransientStatusUpdateService;

class StatusUpdateJobProcessor implements StatusUpdateJob.Processor {

  private final TransientStatusUpdateService statusUpdateService;
  private final StatusUpdateJob job;

  @AssistedInject
  public StatusUpdateJobProcessor(@Assisted StatusUpdateJob job, @Assisted JobControl jobControl, TransientStatusUpdateService statusUpdateService) {
    this.job = job;
    this.statusUpdateService = statusUpdateService;
  }

  @Override
  public Status start() {
    statusUpdateService.send(job.statusUpdate());
    return Status.SUCCESS;
  }

  @Override
  public void setReplacedJob(StatusUpdateJob job) {
    // No-op
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(StatusUpdateJob.Processor.class, StatusUpdateJobProcessor.class)
          .build(StatusUpdateJob.Processor.ProcessorFactory.class));
    }
  }
}