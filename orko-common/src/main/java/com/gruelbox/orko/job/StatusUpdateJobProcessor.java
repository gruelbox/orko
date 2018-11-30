package com.gruelbox.orko.job;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.gruelbox.orko.notification.Status;
import com.gruelbox.orko.notification.TransientStatusUpdateService;
import com.gruelbox.orko.spi.JobControl;

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

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(StatusUpdateJob.Processor.class, StatusUpdateJobProcessor.class)
          .build(StatusUpdateJob.Processor.ProcessorFactory.class));
    }
  }
}