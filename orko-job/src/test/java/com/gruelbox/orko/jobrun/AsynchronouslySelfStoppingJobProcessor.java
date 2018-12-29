package com.gruelbox.orko.jobrun;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

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

class AsynchronouslySelfStoppingJobProcessor implements JobProcessor<AsynchronouslySelfStoppingJob> {

  private volatile AsynchronouslySelfStoppingJob job;
  private final JobControl jobControl;
  private final EventBus eventBus;

  private Disposable subscription1;
  private Disposable subscription2;

  @Inject
  public AsynchronouslySelfStoppingJobProcessor(@Assisted AsynchronouslySelfStoppingJob job, @Assisted JobControl jobControl, EventBus eventBus) {
    this.job = job;
    this.jobControl = jobControl;
    this.eventBus = eventBus;
  }

  @Override
  public Status start() {
    subscription1 = Observable.timer(5, TimeUnit.MILLISECONDS).subscribe(x -> {
      jobControl.finish(Status.SUCCESS);
    });
    subscription2 = Observable.timer(6, TimeUnit.MILLISECONDS).subscribe(x -> {
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

  public interface Factory extends JobProcessor.Factory<AsynchronouslySelfStoppingJob> { }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(new TypeLiteral<JobProcessor<AsynchronouslySelfStoppingJob>>() {}, AsynchronouslySelfStoppingJobProcessor.class)
          .build(AsynchronouslySelfStoppingJobProcessor.class));
    }
  }
}