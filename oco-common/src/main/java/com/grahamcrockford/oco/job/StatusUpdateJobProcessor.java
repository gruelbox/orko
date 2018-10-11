package com.grahamcrockford.oco.job;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.notification.TransientStatusUpdateService;
import com.grahamcrockford.oco.spi.JobControl;

class StatusUpdateJobProcessor implements StatusUpdateJob.Processor {

  private final TransientStatusUpdateService statusUpdateService;
  private final StatusUpdateJob job;

  @AssistedInject
  public StatusUpdateJobProcessor(@Assisted StatusUpdateJob job, @Assisted JobControl jobControl, TransientStatusUpdateService statusUpdateService) {
    this.job = job;
    this.statusUpdateService = statusUpdateService;
  }

  @Override
  public boolean start() {
    statusUpdateService.send(job.statusUpdate());
    return false;
  }

  @Override
  public void stop() {
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(StatusUpdateJob.Processor.class, StatusUpdateJobProcessor.class)
          .build(StatusUpdateJob.Processor.Factory.class));
    }
  }
}