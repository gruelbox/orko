package com.grahamcrockford.oco.job;

import static com.grahamcrockford.oco.notification.Status.SUCCESS;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.notification.Status;
import com.grahamcrockford.oco.notification.TransientNotificationService;
import com.grahamcrockford.oco.spi.JobControl;

class AlertProcessor implements Alert.Processor {

  private final TransientNotificationService notificationService;
  private final Alert job;

  @AssistedInject
  public AlertProcessor(@Assisted Alert job, @Assisted JobControl jobControl, TransientNotificationService notificationService) {
    this.job = job;
    this.notificationService = notificationService;
  }

  @Override
  public Status start() {
    notificationService.send(job.notification());
    return SUCCESS;
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(Alert.Processor.class, AlertProcessor.class)
          .build(Alert.Processor.Factory.class));
    }
  }
}