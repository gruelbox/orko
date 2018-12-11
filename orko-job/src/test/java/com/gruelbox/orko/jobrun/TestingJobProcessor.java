package com.gruelbox.orko.jobrun;

import javax.inject.Inject;

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

class TestingJobProcessor implements JobProcessor<TestingJob> {

  private final TestingJob job;
  private final EventBus eventBus;
  private final JobControl jobControl;
  private volatile boolean done;

  @Inject
  public TestingJobProcessor(@Assisted TestingJob job, @Assisted JobControl jobControl, EventBus eventBus) {
    this.job = job;
    this.jobControl = jobControl;
    this.eventBus = eventBus;
  }

  @Override
  public Status start() {
    if (job.failOnStart())
      throw new IllegalStateException("Fail on start");
    if (job.runAsync()) {
      eventBus.register(this);
      return Status.RUNNING;
    } else {
      eventBus.post(TestingJobEvent.create(job.id(), EventType.FINISH));
      return Status.SUCCESS;
    }
  }

  @Subscribe
  private synchronized void tick(KeepAliveEvent tick) {
    if (done)
      return;
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
    if (job.update())
      jobControl.replace(job.toBuilder().update(false).build());
  }

  @Override
  public void stop() {
    if (job.runAsync()) {
      eventBus.unregister(this);
    }
    eventBus.post(TestingJobEvent.create(job.id(), EventType.FINISH));
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