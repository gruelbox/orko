package com.gruelbox.orko.jobrun;

import org.alfasoftware.morf.upgrade.TableContribution;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

import io.dropwizard.lifecycle.Managed;

public class JobRunModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder<TableContribution> tableContributions = Multibinder.newSetBinder(binder(), TableContribution.class);
    tableContributions.addBinding().to(JobAccessImpl.class);
    tableContributions.addBinding().to(JobLockerImpl.class);
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(JobLockerImpl.class);
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(GuardianLoop.class);
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(JobResource.class);
  }
}