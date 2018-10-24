package com.grahamcrockford.orko.db;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.DBMaker.Maker;
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
  private final Maker dbMaker;
  private final BigDecimal NO_VALUE = new BigDecimal(-999);

  @Inject
  MapDbPermanentSubscriptionAccess(DBMaker.Maker dbMaker) {
    this.dbMaker = dbMaker;
  }

  @Override
  public void add(TickerSpec spec) {
    LOGGER.info("Adding permanent subscription to {}", spec);
    try (DB db = dbMaker.make()) {
      subscriptions(db).putIfAbsent(spec.key(), NO_VALUE);
      db.commit();
    }
  }

  @Override
  public void remove(TickerSpec spec) {
    LOGGER.info("Removing permanent subscription to {}", spec);
    try (DB db = dbMaker.make()) {
      subscriptions(db).remove(spec.key());
      db.commit();
    }
  }

  @Override
  public Set<TickerSpec> all() {
    try (DB db = dbMaker.make()) {
      return FluentIterable.from(subscriptions(db).keySet()).transform(TickerSpec::fromKey).toSet();
    }
  }

  @Override
  public void setReferencePrice(TickerSpec tickerSpec, BigDecimal price) {
    LOGGER.info("Set reference price for {} to {}", tickerSpec, price);
    try (DB db = dbMaker.make()) {
      subscriptions(db).put(tickerSpec.key(), price == null ? NO_VALUE : price);
      db.commit();
    }
  }

  @Override
  public Map<TickerSpec, BigDecimal> getReferencePrices() {
    Map<TickerSpec, BigDecimal> result = new HashMap<TickerSpec, BigDecimal>();
    try (DB db = dbMaker.make()) {
      subscriptions(db).entrySet().stream()
        .filter(e -> !e.getValue().equals(NO_VALUE))
        .forEach(e -> result.put(TickerSpec.fromKey(e.getKey()), e.getValue()));
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private ConcurrentMap<String, BigDecimal> subscriptions(DB db) {
    return db.hashMap("subscription", Serializer.STRING, Serializer.JAVA).createOrOpen();
  }
}