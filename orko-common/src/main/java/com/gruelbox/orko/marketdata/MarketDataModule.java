package com.gruelbox.orko.marketdata;

import org.alfasoftware.morf.upgrade.TableContribution;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.db.EntityContribution;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

import io.dropwizard.lifecycle.Managed;

public class MarketDataModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ExchangeEventRegistry.class).to(ExchangeEventBus.class);
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(MarketDataSubscriptionManager.class);
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(SubscriptionResource.class);

    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(PermanentSubscriptionManager.class);
    Multibinder.newSetBinder(binder(), TableContribution.class).addBinding().to(PermanentSubscriptionAccess.class);
    Multibinder.newSetBinder(binder(), EntityContribution.class).addBinding().to(PermanentSubscriptionAccess.class);
  }
}