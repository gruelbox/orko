package com.grahamcrockford.oco.auth;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.db.DbConfiguration;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

@Singleton
class IpWhitelistAccess {

  private static final int SINGLETON_OBJECT_ID = 1;

  private static final Logger LOGGER = LoggerFactory.getLogger(IpWhitelistAccess.class);

  private final Supplier<DBCollection> collection = Suppliers.memoize(this::createCollection);
  private final MongoClient mongoClient;
  private final DbConfiguration configuration;
  private final AuthConfiguration authConfiguration;

  @Inject
  IpWhitelistAccess(@Nullable MongoClient mongoClient, @Nullable DbConfiguration configuration, AuthConfiguration authConfiguration) {
    this.mongoClient = mongoClient;
    this.configuration = configuration;
    this.authConfiguration = authConfiguration;
  }

  public void setIp(String ip) {
    Preconditions.checkNotNull(ip);
    checkHaveDb();
    try {
      update(ip);
    } catch (DuplicateKeyException e) {
      // Known bug https://stackoverflow.com/questions/30404513/mongodb-update-with-upsert-and-unique-index-atomicity-when-document-is-not-in-th
      update(ip);
    }
  }

  private void update(String ip) {
    collection.get().update(
      new BasicDBObject()
        .append("uid", SINGLETON_OBJECT_ID),
       new BasicDBObject("$set", new BasicDBObject()
           .append("ts", Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)))
           .append("ip", ip)),
      true,
      false
    );
  }

  public void delete(String sourceIp) {
    checkHaveDb();
    collection.get().remove(new BasicDBObject().append("uid", SINGLETON_OBJECT_ID));
  }

  public String getIp() {
    checkHaveDb();
    DBObject result = collection.get().findOne(new BasicDBObject().append("uid", SINGLETON_OBJECT_ID));
    if (result == null)
      return null;
    return (String) result.get("ip");
  }

  private DBCollection createCollection() {
    DBCollection collection = mongoClient.getDB(configuration.getMongoDatabase()).getCollection("ipwl");
    createUniqueIndex(collection);
    createTtlIndex(collection);
    return collection;
  }

  private void createUniqueIndex(DBCollection collection) {
    BasicDBObject index = new BasicDBObject().append("uid", 1);
    BasicDBObject indexOpts = new BasicDBObject()
        .append("name", "unq")
        .append("unique", true);
    collection.createIndex(index, indexOpts);
  }

  private void createTtlIndex(DBCollection collection) {
    BasicDBObject index = new BasicDBObject().append("ts", 1);
    BasicDBObject indexOpts = new BasicDBObject()
        .append("name", "ttl")
        .append("expireAfterSeconds", MoreObjects.firstNonNull(authConfiguration.whitelistExpirySeconds, 7200));
    try {
      collection.createIndex(index, indexOpts);
    } catch (MongoException e) {
      LOGGER.warn("TTL index failed to be created ({}). Dropping existing TTL indexes", e.getMessage());
      safeDropIndex(collection, "ts_1");
      safeDropIndex(collection, "ttl");
      collection.createIndex(index, indexOpts);
      LOGGER.info("TTL index recreated successfully");
    }
  }

  private void safeDropIndex(DBCollection coll, String indexName) {
    try {
      coll.dropIndex(indexName);
      LOGGER.info("Dropped {} index", indexName);
    } catch (MongoException e) {
      LOGGER.info("Failed to drop {} index ({})", indexName, e.getMessage());
    }
  }

  private void checkHaveDb() {
    if (mongoClient == null) {
      throw new IllegalStateException("IP whitelisting support only available when a database is provided");
    }
  }
}