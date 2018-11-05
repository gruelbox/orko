package com.grahamcrockford.orko.worker;

import com.google.inject.AbstractModule;
import com.grahamcrockford.orko.guardian.GuardianModule;
import com.grahamcrockford.orko.mq.MqModule;

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