package com.grahamcrockford.orko.db;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.exchange.ExchangeService;
import com.grahamcrockford.orko.marketdata.PermanentSubscriptionAccess;
import com.grahamcrockford.orko.notification.NotificationService;
import com.grahamcrockford.orko.spi.TickerSpec;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import jersey.repackaged.com.google.common.collect.Maps;

@Singleton
class MongoPermanentSubscriptionAccess implements PermanentSubscriptionAccess {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoPermanentSubscriptionAccess.class);

  private final Supplier<JacksonDBCollection<DbSubscription2, String>> collection = Suppliers.memoize(this::collection);

  private final MongoClient mongoClient;
  private final DbConfiguration configuration;

  @Inject
  MongoPermanentSubscriptionAccess(MongoClient mongoClient, DbConfiguration configuration, ExchangeService exchangeService, NotificationService notificationService) {
    this.mongoClient = mongoClient;
    this.configuration = configuration;
    all().forEach(sub -> {
      try {
        if (!exchangeService.exchangeSupportsPair(sub.exchange(), sub.currencyPair())) {
          notificationService.error("Removing permanent subscription as currency no longer supported: " + sub);
          remove(sub);
        }
      } catch (Exception e) {
        LOGGER.error("Failed to check exchange for existence of ticker: " + sub, e);
      }
    });
  }

  @Override
  public void add(TickerSpec spec) {
    collection.get().insert(DbSubscription2.create(spec));
  }

  @Override
  public void remove(TickerSpec spec) {
    collection.get().removeById(spec.key());
  }

  @Override
  public Set<TickerSpec> all() {
    return FluentIterable.from(collection.get().find()).transform(sub -> sub.spec()).toSet();
  }

  @Override
  public void setReferencePrice(TickerSpec tickerSpec, BigDecimal price) {
    collection.get().update(DBQuery.is("_id", tickerSpec.key()), DbSubscription2.create(tickerSpec.key(), tickerSpec, price == null ? null : price.toPlainString()), true, false);
  }

  @Override
  public Map<TickerSpec, BigDecimal> getReferencePrices() {
    Iterator<DbSubscription2> subscriptions = collection.get().find();
    ImmutableMap<TickerSpec, DbSubscription2> indexed = Maps.uniqueIndex(subscriptions, (DbSubscription2 v) -> v.spec());
    Map<TickerSpec, DbSubscription2> noNulls = Maps.filterValues(indexed, v -> v.referencePrice() != null);
    return Maps.transformValues(noNulls, sub -> new BigDecimal(sub.referencePrice()));
  }

  private JacksonDBCollection<DbSubscription2, String> collection() {
    DBCollection result = mongoClient.getDB(configuration.getMongoDatabase()).getCollection("subscription2");
    return JacksonDBCollection.wrap(result, DbSubscription2.class, String.class);
  }
}