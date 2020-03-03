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
package com.gruelbox.orko.jobrun;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.gruelbox.orko.jobrun.TestingJobEvent.EventType;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.JobProcessor;
import com.gruelbox.orko.jobrun.spi.Status;
import javax.inject.Inject;

class TestingJobProcessor implements JobProcessor<TestingJob> {

  private final EventBus eventBus;
  private final JobControl jobControl;

  private volatile TestingJob job;
  private volatile boolean done;

  @Inject
  public TestingJobProcessor(
      @Assisted TestingJob job, @Assisted JobControl jobControl, EventBus eventBus) {
    this.job = job;
    this.jobControl = jobControl;
    this.eventBus = eventBus;
  }

  @Override
  public Status start() {
    if (job.failOnStart()) throw new IllegalStateException("Fail on start");
    if (job.runAsync()) {
      eventBus.register(this);
      return Status.RUNNING;
    } else {
      eventBus.post(TestingJobEvent.create(job.id(), EventType.FINISH));
      return Status.SUCCESS;
    }
  }

  @Override
  public void setReplacedJob(TestingJob job) {
    this.job = job;
  }

  @Subscribe
  private synchronized void tick(KeepAliveEvent tick) {
    if (done) return;
    eventBus.post(TestingJobEvent.create(job.id(), EventType.START));
    if (job.failOnTick()) {
      done = true;
      jobControl.finish(Status.FAILURE_PERMANENT);
      return;
    }
    if (!job.stayResident()) {
      done = true;
      jobControl.finish(Status.SUCCESS);
    }
    if (job.update()) jobControl.replace(job.toBuilder().update(false).build());
  }

  @Override
  public void stop() {
    if (job.runAsync()) {
      eventBus.unregister(this);
    }
    eventBus.post(TestingJobEvent.create(job.id(), EventType.FINISH));
    if (job.failOnStop()) throw new IllegalStateException("Fail on stop");
  }

  public interface Factory extends JobProcessor.Factory<TestingJob> {}

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(
          new FactoryModuleBuilder()
              .implement(new TypeLiteral<JobProcessor<TestingJob>>() {}, TestingJobProcessor.class)
              .build(TestingJobProcessor.class));
    }
  }
}
