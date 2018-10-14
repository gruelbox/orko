package com.grahamcrockford.oco.notification;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class StatusUpdateServiceImpl implements TransientStatusUpdateService {

  private final EventBus eventBus;

  @Inject
  StatusUpdateServiceImpl(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public void send(StatusUpdate statusUpdate) {
    eventBus.post(statusUpdate);
  }
}