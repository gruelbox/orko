package com.grahamcrockford.oco.strategy;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.notification.NotificationService;
import com.grahamcrockford.oco.signal.UserTradeEvent;

import io.dropwizard.lifecycle.Managed;

@Singleton
class UserTradeNotifier implements Managed {

  private final EventBus eventBus;
  private final NotificationService notificationService;

  @Inject
  UserTradeNotifier(EventBus eventBus, NotificationService notificationService) {
    this.eventBus = eventBus;
    this.notificationService = notificationService;
  }

  @Override
  public void start() throws Exception {
    eventBus.register(this);
  }

  @Override
  public void stop() throws Exception {
    eventBus.unregister(this);
  }

  @Subscribe
  void onUserTrade(UserTradeEvent e) {
    String message = String.format(
      "Trade executed on %s %s/%s market: %s %s at %s",
      e.spec().exchange(),
      e.spec().base(),
      e.spec().counter(),
      e.trade().getType().toString().toLowerCase(),
      e.trade().getOriginalAmount(),
      e.trade().getPrice()
    );
    notificationService.info(message);
  }
}