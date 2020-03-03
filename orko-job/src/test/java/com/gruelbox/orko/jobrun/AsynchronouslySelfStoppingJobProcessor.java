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

class AsynchronouslySelfStoppingJobProcessor
    implements JobProcessor<AsynchronouslySelfStoppingJob> {

  private volatile AsynchronouslySelfStoppingJob job;
  private final JobControl jobControl;
  private final EventBus eventBus;

  private Disposable subscription1;
  private Disposable subscription2;

  @Inject
  public AsynchronouslySelfStoppingJobProcessor(
      @Assisted AsynchronouslySelfStoppingJob job,
      @Assisted JobControl jobControl,
      EventBus eventBus) {
    this.job = job;
    this.jobControl = jobControl;
    this.eventBus = eventBus;
  }

  @Override
  public Status start() {
    subscription1 =
        Observable.timer(5, TimeUnit.MILLISECONDS)
            .subscribe(
                x -> {
                  jobControl.finish(Status.SUCCESS);
                });
    subscription2 =
        Observable.timer(6, TimeUnit.MILLISECONDS)
            .subscribe(
                x -> {
                  jobControl.replace(job);
                });
    eventBus.post(TestingJobEvent.create(job.id(), EventType.START));
    return Status.RUNNING;
  }

  @Override
  public void setReplacedJob(AsynchronouslySelfStoppingJob job) {
    this.job = job;
  }

  @Override
  public void stop() {
    SafelyDispose.of(subscription1, subscription2);
    eventBus.post(TestingJobEvent.create(job.id(), EventType.FINISH));
  }

  public interface Factory extends JobProcessor.Factory<AsynchronouslySelfStoppingJob> {}

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(
          new FactoryModuleBuilder()
              .implement(
                  new TypeLiteral<JobProcessor<AsynchronouslySelfStoppingJob>>() {},
                  AsynchronouslySelfStoppingJobProcessor.class)
              .build(AsynchronouslySelfStoppingJobProcessor.class));
    }
  }
}
