package com.grahamcrockford.oco.marketdata;

import static com.grahamcrockford.oco.marketdata.MarketDataType.BALANCE;
import static com.grahamcrockford.oco.marketdata.MarketDataType.OPEN_ORDERS;
import static com.grahamcrockford.oco.marketdata.MarketDataType.ORDERBOOK;
import static com.grahamcrockford.oco.marketdata.MarketDataType.TICKER;
import static com.grahamcrockford.oco.marketdata.MarketDataType.TRADES;
import static com.grahamcrockford.oco.marketdata.MarketDataType.USER_TRADE_HISTORY;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.spi.TickerSpec;

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
    Set<MarketDataSubscription> all = FluentIterable.from(permanentSubscriptionAccess.all()).transformAndConcat(this::subscriptionsFor).toSet();
    LOGGER.info("Updating permanent subscriptions to {}", all);
    exchangeEventRegistry.changeSubscriptions(getClass().getSimpleName(), all);
  }

  public void add(TickerSpec spec) {
    permanentSubscriptionAccess.add(spec);
    update();
  }

  public void remove(TickerSpec spec) {
    permanentSubscriptionAccess.remove(spec);
    update();
  }

  public Collection<TickerSpec> all() {
    return permanentSubscriptionAccess.all();
  }

  public void setReferencePrice(TickerSpec tickerSpec, BigDecimal price) {
    permanentSubscriptionAccess.setReferencePrice(tickerSpec, price);
  }

  public Map<TickerSpec, BigDecimal> referencePrices() {
    return permanentSubscriptionAccess.getReferencePrices();
  }

  private Collection<MarketDataSubscription> subscriptionsFor(TickerSpec spec) {
    return ImmutableList.of(
      MarketDataSubscription.create(spec, TICKER),
      MarketDataSubscription.create(spec, ORDERBOOK),
      MarketDataSubscription.create(spec, OPEN_ORDERS),
      MarketDataSubscription.create(spec, USER_TRADE_HISTORY),
      MarketDataSubscription.create(spec, BALANCE),
      MarketDataSubscription.create(spec, TRADES)
    );
  }
}