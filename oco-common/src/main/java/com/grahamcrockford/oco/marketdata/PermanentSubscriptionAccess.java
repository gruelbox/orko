package com.grahamcrockford.oco.marketdata;

import java.util.Set;

public interface PermanentSubscriptionAccess {

  public void add(MarketDataSubscription... subscription);

  public void remove(MarketDataSubscription... subscription);

  public Set<MarketDataSubscription> all();

}