/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.subscription;

import static com.gruelbox.orko.exchange.MarketDataType.BALANCE;
import static com.gruelbox.orko.exchange.MarketDataType.OPEN_ORDERS;
import static com.gruelbox.orko.exchange.MarketDataType.ORDER;
import static com.gruelbox.orko.exchange.MarketDataType.ORDERBOOK;
import static com.gruelbox.orko.exchange.MarketDataType.TICKER;
import static com.gruelbox.orko.exchange.MarketDataType.TRADES;
import static com.gruelbox.orko.exchange.MarketDataType.USER_TRADE;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.exchange.ExchangeEventRegistry;
import com.gruelbox.orko.exchange.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.exchange.MarketDataSubscription;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.SafelyClose;
import io.dropwizard.lifecycle.Managed;
import java.util.Collection;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class SubscriptionManager implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionManager.class);

  private final SubscriptionAccess subscriptionAccess;
  private final ExchangeEventRegistry exchangeEventRegistry;
  private final Transactionally transactionally;

  private volatile ExchangeEventSubscription subscription;

  @Inject
  SubscriptionManager(
      SubscriptionAccess subscriptionAccess,
      ExchangeEventRegistry exchangeEventRegistry,
      Transactionally transactionally) {
    this.subscriptionAccess = subscriptionAccess;
    this.exchangeEventRegistry = exchangeEventRegistry;
    this.transactionally = transactionally;
  }

  @Override
  public void start() throws Exception {
    subscription = exchangeEventRegistry.subscribe();
    transactionally.run(Transactionally.READ_ONLY_UNIT, this::update);
  }

  @Override
  public void stop() throws Exception {
    SafelyClose.the(subscription);
  }

  private void update() {
    Set<MarketDataSubscription> all =
        FluentIterable.from(subscriptionAccess.all())
            .transformAndConcat(this::subscriptionsFor)
            .toSet();
    LOGGER.info("Updating permanent subscriptions to {}", all);
    subscription = subscription.replace(all);
  }

  void add(TickerSpec spec) {
    subscriptionAccess.add(spec);
    update();
  }

  void remove(TickerSpec spec) {
    subscriptionAccess.remove(spec);
    update();
  }

  private Collection<MarketDataSubscription> subscriptionsFor(TickerSpec spec) {
    return ImmutableList.of(
        MarketDataSubscription.create(spec, TICKER),
        MarketDataSubscription.create(spec, ORDERBOOK),
        MarketDataSubscription.create(spec, OPEN_ORDERS),
        MarketDataSubscription.create(spec, USER_TRADE),
        MarketDataSubscription.create(spec, BALANCE),
        MarketDataSubscription.create(spec, TRADES),
        MarketDataSubscription.create(spec, ORDER));
  }
}
