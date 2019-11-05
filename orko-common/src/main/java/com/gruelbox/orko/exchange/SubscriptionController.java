package com.gruelbox.orko.exchange;

import java.util.Set;

interface SubscriptionController {

  /**
   * Changes the market data currently being published.
   *
   * @param subscriptions The subscriptions.
   */
  void updateSubscriptions(Set<MarketDataSubscription> subscriptions);

}
