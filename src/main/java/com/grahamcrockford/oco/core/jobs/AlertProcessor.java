package com.grahamcrockford.oco.core.jobs;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.core.spi.JobControl;
import com.grahamcrockford.oco.core.spi.JobProcessor;
import com.grahamcrockford.oco.telegram.TelegramService;

class AlertProcessor implements JobProcessor<Alert> {

  private final TelegramService telegramService;
  private final Alert job;

  @AssistedInject
  public AlertProcessor(@Assisted Alert job, @Assisted JobControl jobControl, TelegramService telegramService) {
    this.job = job;
    this.telegramService = telegramService;
  }

  @Override
  public boolean start() {
    telegramService.sendMessage(job.message());
    return false;
  }

  @Override
  public void stop() {
  }

  public interface Factory extends JobProcessor.Factory<Alert> { }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(new TypeLiteral<JobProcessor<Alert>>() {}, AlertProcessor.class)
          .build(Factory.class));
    }
  }
}