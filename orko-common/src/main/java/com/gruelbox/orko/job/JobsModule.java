package com.gruelbox.orko.job;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

public class JobsModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new LimitOrderJobProcessor.Module());
    install(new OneCancelsOtherProcessor.Module());
    install(new SoftTrailingStopProcessor.Module());
    install(new AlertProcessor.Module());
    install(new StatusUpdateJobProcessor.Module());
    install(new ScriptJobProcessor.Module());
    Multibinder.newSetBinder(binder(), WebResource.class)
      .addBinding().to(ScriptResource.class);
  }
}
