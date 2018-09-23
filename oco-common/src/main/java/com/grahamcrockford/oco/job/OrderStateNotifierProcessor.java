package com.grahamcrockford.oco.job;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.notification.NotificationService;

@Deprecated
class OrderStateNotifierProcessor implements OrderStateNotifier.Processor {

  private final OrderStateNotifier job;
  private final NotificationService notificationService;

  @AssistedInject
  public OrderStateNotifierProcessor(@Assisted OrderStateNotifier job,
                                     final NotificationService notificationService) {
    this.job = job;
    this.notificationService = notificationService;
  }

  @Override
  public boolean start() {
    notificationService.info("Discarded deprecated job: " + job);
    return false;
  }

  @Override
  public void stop() {
    // No-op
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