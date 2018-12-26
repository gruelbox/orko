package com.gruelbox.orko.job;

import com.google.inject.AbstractModule;
import com.gruelbox.orko.job.script.ScriptModule;

public class JobsModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new LimitOrderJobProcessor.Module());
    install(new OneCancelsOtherProcessor.Module());
    install(new SoftTrailingStopProcessor.Module());
    install(new AlertProcessor.Module());
    install(new StatusUpdateJobProcessor.Module());
    install(new ScriptModule());
  }
}
