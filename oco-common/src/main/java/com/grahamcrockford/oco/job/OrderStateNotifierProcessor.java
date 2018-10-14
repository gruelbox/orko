package com.grahamcrockford.oco.job;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.notification.NotificationService;
import com.grahamcrockford.oco.notification.Status;
import com.grahamcrockford.oco.spi.JobControl;

@Deprecated
class OrderStateNotifierProcessor implements OrderStateNotifier.Processor {

  private final OrderStateNotifier job;
  private final NotificationService notificationService;

  @AssistedInject
  public OrderStateNotifierProcessor(@Assisted OrderStateNotifier job,
                                     @Assisted JobControl jobControl,
                                     final NotificationService notificationService) {
    this.job = job;
    this.notificationService = notificationService;
  }

  @Override
  public Status start() {
    notificationService.info("Discarded deprecated job: " + job);
    return Status.FAILURE_PERMANENT;
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(OrderStateNotifier.Processor.class, OrderStateNotifierProcessor.class)
          .build(OrderStateNotifier.Processor.Factory.class));
    }
  }
}