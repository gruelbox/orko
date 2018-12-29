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

class CounterJobProcessor implements JobProcessor<CounterJob> {

  private final JobControl jobControl;
  private final EventBus eventBus;

  private Disposable subscription;

  private volatile CounterJob job;
  private volatile boolean done;

  @Inject
  public CounterJobProcessor(@Assisted CounterJob job, @Assisted JobControl jobControl, EventBus eventBus) {
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
    if (done)
      return;
    if (job.counter() == 100) {
      done = true;
      jobControl.finish(Status.SUCCESS);
      return;
    }
    jobControl.replace(job.toBuilder().counter(job.counter() + 1).build());
  }

  @Override
  public void setReplacedJob(CounterJob job) {
    eventBus.post(new Integer(job.counter()));
    this.job = job;
  }

  @Override
  public void stop() {
    SafelyDispose.of(subscription);
    eventBus.post(TestingJobEvent.create(job.id(), EventType.FINISH));
  }

  public interface Factory extends JobProcessor.Factory<CounterJob> { }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(new TypeLiteral<JobProcessor<CounterJob>>() {}, CounterJobProcessor.class)
          .build(CounterJobProcessor.class));
    }
  }
}