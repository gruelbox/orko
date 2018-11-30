package com.gruelbox.orko.job;

import com.google.inject.AbstractModule;

public class JobsModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new LimitOrderJobProcessor.Module());
    install(new OneCancelsOtherProcessor.Module());
    install(new SoftTrailingStopProcessor.Module());
    install(new AlertProcessor.Module());
    install(new StatusUpdateJobProcessor.Module());
  }
}
