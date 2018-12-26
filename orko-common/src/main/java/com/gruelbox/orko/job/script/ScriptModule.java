package com.gruelbox.orko.job.script;

import org.alfasoftware.morf.upgrade.TableContribution;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.db.EntityContribution;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

public class ScriptModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new ScriptJobProcessor.Module());
    Multibinder.newSetBinder(binder(), WebResource.class)
      .addBinding().to(ScriptResource.class);
    Multibinder.newSetBinder(binder(), EntityContribution.class)
      .addBinding().to(ScriptContribution.class);
    Multibinder.newSetBinder(binder(), TableContribution.class)
      .addBinding().to(ScriptContribution.class);
  }
}
