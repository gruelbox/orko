package com.grahamcrockford.oco.core.impl;

import javax.inject.Inject;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.core.spi.JobControl;
import com.grahamcrockford.oco.core.spi.JobProcessor;
import com.grahamcrockford.oco.core.spi.KeepAliveEvent;

class TestingJobProcessor implements JobProcessor<TestingJob> {

  private final TestingJob job;
  private final AsyncEventBus asyncEventBus;
  private final JobControl jobControl;

  @Inject
  public TestingJobProcessor(@Assisted TestingJob job, @Assisted JobControl jobControl, AsyncEventBus asyncEventBus) {
    this.job = job;
    this.jobControl = jobControl;
    this.asyncEventBus = asyncEventBus;
  }

  @Override
  public boolean start() {
    if (job.runAsync()) {
      asyncEventBus.register(this);
      return true;
    } else {
      if (job.startLatch() != null) {
        job.startLatch().countDown();
      }
      return false;
    }
  }

  @Subscribe
  private void tick(KeepAliveEvent tick) {
    if (job.startLatch() != null)
      job.startLatch().countDown();
    if (!job.stayResident())
      jobControl.finish();
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