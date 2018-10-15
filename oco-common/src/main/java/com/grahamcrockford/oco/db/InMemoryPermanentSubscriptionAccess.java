package com.grahamcrockford.oco.db;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.marketdata.PermanentSubscriptionAccess;
import com.grahamcrockford.oco.spi.TickerSpec;

@Singleton
public class InMemoryPermanentSubscriptionAccess implements PermanentSubscriptionAccess {

  private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryPermanentSubscriptionAccess.class);
  private final ConcurrentMap<TickerSpec, Optional<BigDecimal>> subscriptions = Maps.newConcurrentMap();

  InMemoryPermanentSubscriptionAccess() {
    subscriptions.put(TickerSpec.builder().base("BTC").counter("USDT").exchange("binance").build(), Optional.empty());
    subscriptions.put(TickerSpec.builder().base("ETH").counter("USDT").exchange("binance").build(), Optional.empty());
    subscriptions.put(TickerSpec.builder().base("NEO").counter("USDT").exchange("binance").build(), Optional.empty());
    subscriptions.put(TickerSpec.builder().base("XRP").counter("USDT").exchange("binance").build(), Optional.empty());
    subscriptions.put(TickerSpec.builder().base("ETH").counter("BTC").exchange("binance").build(), Optional.empty());
  }

  @Override
  public void add(TickerSpec spec) {
    LOGGER.info("Adding permanent subscription to {}", spec);
    subscriptions.putIfAbsent(spec, Optional.empty());
  }

  @Override
  public void remove(TickerSpec spec) {
    LOGGER.info("Removing permanent subscription to {}", spec);
    subscriptions.remove(spec);
  }

  @Override
  public Set<TickerSpec> all() {
    return subscriptions.keySet();
  }

  @Override
  public void setReferencePrice(TickerSpec tickerSpec, BigDecimal price) {
    LOGGER.info("Set reference price for {} to {}", tickerSpec, price);
    subscriptions.put(tickerSpec, Optional.ofNullable(price));
  }

  @Override
  public Map<TickerSpec, BigDecimal> getReferencePrices() {
    return Maps.filterEntries(Maps.transformValues(subscriptions, o -> o.orElse(null)), e -> e != null);
  }
}