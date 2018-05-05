package com.grahamcrockford.oco.job;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.notification.NotificationService;
import com.grahamcrockford.oco.spi.JobControl;

class AlertProcessor implements Alert.Processor {

  private final NotificationService notificationService;
  private final Alert job;

  @AssistedInject
  public AlertProcessor(@Assisted Alert job, @Assisted JobControl jobControl, NotificationService notificationService) {
    this.job = job;
    this.notificationService = notificationService;
  }

  @Override
  public boolean start() {
    notificationService.info(job.message());
    return false;
  }

  @Override
  public void stop() {
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