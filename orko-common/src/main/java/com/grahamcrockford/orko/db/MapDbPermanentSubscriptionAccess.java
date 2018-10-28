package com.grahamcrockford.orko.db;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.marketdata.PermanentSubscriptionAccess;
import com.grahamcrockford.orko.spi.TickerSpec;

@Singleton
public class MapDbPermanentSubscriptionAccess implements PermanentSubscriptionAccess {

  private static final Logger LOGGER = LoggerFactory.getLogger(MapDbPermanentSubscriptionAccess.class);
  private static final BigDecimal NO_VALUE = new BigDecimal(-999);
  private final HTreeMap<String, BigDecimal> subscriptions;
  private final DB db;

  @SuppressWarnings("unchecked")
  @Inject
  MapDbPermanentSubscriptionAccess(MapDbMakerFactory dbMakerFactory) {
    this.db = dbMakerFactory.create("subscriptions").make();
    this.subscriptions = db.hashMap("subscription", Serializer.STRING, Serializer.JAVA).createOrOpen();
  }

  @Override
  public void add(TickerSpec spec) {
    LOGGER.info("Adding permanent subscription to {}", spec);
    subscriptions.putIfAbsent(spec.key(), NO_VALUE);
    db.commit();
  }

  @Override
  public void remove(TickerSpec spec) {
    LOGGER.info("Removing permanent subscription to {}", spec);
    subscriptions.remove(spec.key());
    db.commit();
  }

  @Override
  public Set<TickerSpec> all() {
    return FluentIterable.from(subscriptions.keySet()).transform(TickerSpec::fromKey).toSet();
  }

  @Override
  public void setReferencePrice(TickerSpec tickerSpec, BigDecimal price) {
    LOGGER.info("Set reference price for {} to {}", tickerSpec, price);
    subscriptions.put(tickerSpec.key(), price == null ? NO_VALUE : price);
    db.commit();
  }

  @Override
  public Map<TickerSpec, BigDecimal> getReferencePrices() {
    Map<TickerSpec, BigDecimal> result = new HashMap<TickerSpec, BigDecimal>();
    subscriptions.entrySet().stream()
      .filter(e -> !e.getValue().equals(NO_VALUE))
      .forEach(e -> result.put(TickerSpec.fromKey(e.getKey()), e.getValue()));
    return result;
  }
}