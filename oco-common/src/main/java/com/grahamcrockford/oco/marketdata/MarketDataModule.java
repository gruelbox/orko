package com.grahamcrockford.oco.marketdata;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.wiring.WebResource;

public class MarketDataModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ExchangeEventRegistry.class).to(ExchangeEventBus.class);
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(MarketDataSubscriptionManager.class);
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(SubscriptionResource.class);
  }
}