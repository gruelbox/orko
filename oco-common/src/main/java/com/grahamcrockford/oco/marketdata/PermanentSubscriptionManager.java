package com.grahamcrockford.oco.marketdata;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.dropwizard.lifecycle.Managed;

@Singleton
class PermanentSubscriptionManager implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(PermanentSubscriptionManager.class);

  private final PermanentSubscriptionAccess permanentSubscriptionAccess;
  private final ExchangeEventRegistry exchangeEventRegistry;

  @Inject
  PermanentSubscriptionManager(PermanentSubscriptionAccess permanentSubscriptionAccess,
                               ExchangeEventRegistry exchangeEventRegistry) {
    this.permanentSubscriptionAccess = permanentSubscriptionAccess;
    this.exchangeEventRegistry = exchangeEventRegistry;
  }

  @Override
  public void start() throws Exception {
    update();
  }

  @Override
  public void stop() throws Exception {
    // No-op
  }

  private void update() {
    Set<MarketDataSubscription> all = permanentSubscriptionAccess.all();
    LOGGER.info("Updating permanent subscriptions to {}", all);
    exchangeEventRegistry.changeSubscriptions(getClass().getSimpleName(), all);
  }

  public void add(MarketDataSubscription... subscription) {
    permanentSubscriptionAccess.add(subscription);
    update();
  }

  public void remove(MarketDataSubscription... subscription) {
    permanentSubscriptionAccess.remove(subscription);
    update();
  }

  public Set<MarketDataSubscription> all() {
    return permanentSubscriptionAccess.all();
  }
}