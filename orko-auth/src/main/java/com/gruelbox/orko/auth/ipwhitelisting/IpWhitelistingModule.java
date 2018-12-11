package com.gruelbox.orko.auth.ipwhitelisting;

import org.alfasoftware.morf.upgrade.TableContribution;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.db.EntityContribution;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

import io.dropwizard.lifecycle.Managed;

public class IpWhitelistingModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(IpWhitelistingResource.class);

    Multibinder.newSetBinder(binder(), TableContribution.class).addBinding().to(IpWhitelistContribution.class);
    Multibinder.newSetBinder(binder(), EntityContribution.class).addBinding().to(IpWhitelistContribution.class);
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(IpWhitelistAccessImpl.class);
  }
}