package com.grahamcrockford.orko.submit;

import org.alfasoftware.morf.upgrade.TableContribution;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import io.dropwizard.lifecycle.Managed;

public class SubmitModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder<TableContribution> tableContributions = Multibinder.newSetBinder(binder(), TableContribution.class);
    tableContributions.addBinding().to(JobAccessImpl.class);
    tableContributions.addBinding().to(JobLockerImpl.class);
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(JobLockerImpl.class);
  }
}
