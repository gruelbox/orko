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
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.gruelbox.orko.jobrun.TestingJobEvent.EventType;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.JobProcessor;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.util.SafelyDispose;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

class CounterJobProcessor implements JobProcessor<CounterJob> {

  private final JobControl jobControl;
  private final EventBus eventBus;

  private Disposable subscription;

  private volatile CounterJob job;
  private volatile boolean done;

  @Inject
  public CounterJobProcessor(
      @Assisted CounterJob job, @Assisted JobControl jobControl, EventBus eventBus) {
    this.job = job;
    this.jobControl = jobControl;
    this.eventBus = eventBus;
  }

  @Override
  public Status start() {
    subscription = Observable.interval(5, TimeUnit.MILLISECONDS).subscribe(x -> tick());
    eventBus.post(TestingJobEvent.create(job.id(), EventType.START));
    return Status.RUNNING;
  }

  private synchronized void tick() {
    if (done) return;
    if (job.counter() == 100) {
      done = true;
      jobControl.finish(Status.SUCCESS);
      return;
    }
    jobControl.replace(job.toBuilder().counter(job.counter() + 1).build());
  }

  @Override
  public void setReplacedJob(CounterJob job) {
    eventBus.post(job.counter());
    this.job = job;
  }

  @Override
  public void stop() {
    SafelyDispose.of(subscription);
    eventBus.post(TestingJobEvent.create(job.id(), EventType.FINISH));
  }

  public interface Factory extends JobProcessor.Factory<CounterJob> {}

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(
          new FactoryModuleBuilder()
              .implement(new TypeLiteral<JobProcessor<CounterJob>>() {}, CounterJobProcessor.class)
              .build(CounterJobProcessor.class));
    }
  }
}
