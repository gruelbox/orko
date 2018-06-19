package com.grahamcrockford.oco.db;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

import org.mongojack.JacksonDBCollection;

import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.marketdata.MarketDataSubscription;
import com.grahamcrockford.oco.marketdata.PermanentSubscriptionAccess;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

@Singleton
class DbPermanentSubscriptionAccess implements PermanentSubscriptionAccess {

  private final Supplier<JacksonDBCollection<DbSubscription, String>> collection = Suppliers.memoize(this::collection);

  private final MongoClient mongoClient;
  private final DbConfiguration configuration;

  @Inject
  DbPermanentSubscriptionAccess(MongoClient mongoClient, DbConfiguration configuration) {
    this.mongoClient = mongoClient;
    this.configuration = configuration;
  }

  @Override
  public void add(MarketDataSubscription... subscription) {
    collection.get().insert(FluentIterable.from(subscription).transform(DbSubscription::create).toList());
  }

  @Override
  public void remove(MarketDataSubscription... subscription) {
    Arrays.asList(subscription).forEach(s -> collection.get().removeById(s.key()));
  }

  @Override
  public Set<MarketDataSubscription> all() {
    return FluentIterable.from(collection.get().find()).transform(DbSubscription::toSubscription).toSet();
  }

  private JacksonDBCollection<DbSubscription, String> collection() {
    DBCollection collection = mongoClient.getDB(configuration.getMongoDatabase()).getCollection("subscription");
    return JacksonDBCollection.wrap(collection, DbSubscription.class, String.class);
  }
}