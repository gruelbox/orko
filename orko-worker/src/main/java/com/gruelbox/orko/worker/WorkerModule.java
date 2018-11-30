package com.gruelbox.orko.worker;

import com.google.inject.AbstractModule;
import com.gruelbox.orko.guardian.GuardianModule;
import com.gruelbox.orko.mq.MqModule;

/**
 * Top level bindings.
 */
class WorkerModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new MqModule());
    install(new GuardianModule(true));
  }
}