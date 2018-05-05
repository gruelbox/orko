package com.grahamcrockford.oco.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.notification.NotificationService;
import com.grahamcrockford.oco.spi.JobControl;

class AlertProcessor implements Alert.Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(AlertProcessor.class);

  private final NotificationService telegramService;
  private final Alert job;

  @AssistedInject
  public AlertProcessor(@Assisted Alert job, @Assisted JobControl jobControl, NotificationService telegramService) {
    this.job = job;
    this.telegramService = telegramService;
  }

  @Override
  public boolean start() {
    LOGGER.info("Sending message: " + job.message());
    telegramService.sendMessage(job.message());
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