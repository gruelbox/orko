package com.gruelbox.orko.notification;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.jobrun.spi.StatusUpdate;

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