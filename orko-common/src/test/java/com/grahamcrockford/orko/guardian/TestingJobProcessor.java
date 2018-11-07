package com.grahamcrockford.orko.guardian;

import javax.inject.Inject;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.orko.notification.Status;
import com.grahamcrockford.orko.spi.JobControl;
import com.grahamcrockford.orko.spi.JobProcessor;

class TestingJobProcessor implements JobProcessor<TestingJob> {

  private final TestingJob job;
  private final EventBus asyncEventBus;
  private final JobControl jobControl;
  private volatile boolean done;

  @Inject
  public TestingJobProcessor(@Assisted TestingJob job, @Assisted JobControl jobControl, EventBus asyncEventBus) {
    this.job = job;
    this.jobControl = jobControl;
    this.asyncEventBus = asyncEventBus;
  }

  @Override
  public Status start() {
    if (job.failOnStart())
      throw new IllegalStateException("Fail on start");
    if (job.runAsync()) {
      asyncEventBus.register(this);
      return Status.RUNNING;
    } else {
      if (job.startLatch() != null) {
        job.startLatch().countDown();
      }
      return Status.SUCCESS;
    }
  }

  @Subscribe
  private synchronized void tick(KeepAliveEvent tick) {
    if (done)
      return;
    if (job.startLatch() != null)
      job.startLatch().countDown();
    if (job.failOnTick()) {
      done = true;
      jobControl.finish(Status.FAILURE_PERMANENT);
      return;
    }
    if (!job.stayResident()) {
      done = true;
      jobControl.finish(Status.SUCCESS);
    }
    if (job.update())
      jobControl.replace(job.toBuilder().update(false).build());
  }

  @Override
  public void stop() {
    if (job.runAsync()) {
      asyncEventBus.unregister(this);
    }
    if (job.completionLatch() != null)
      job.completionLatch().countDown();
    if (job.failOnStop())
      throw new IllegalStateException("Fail on stop");
  }

  public interface Factory extends JobProcessor.Factory<TestingJob> { }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(new TypeLiteral<JobProcessor<TestingJob>>() {}, TestingJobProcessor.class)
          .build(TestingJobProcessor.class));
    }
  }
}