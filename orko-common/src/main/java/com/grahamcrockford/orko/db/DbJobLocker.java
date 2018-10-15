package com.grahamcrockford.orko.db;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.submit.JobLocker;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

@Singleton
class DbJobLocker implements JobLocker {

  private static final Logger LOGGER = LoggerFactory.getLogger(DbJobLocker.class);

  private final Supplier<DBCollection> lock = Suppliers.memoize(this::createLockCollection);
  private final MongoClient mongoClient;
  private final DbConfiguration configuration;

  @Inject
  DbJobLocker(MongoClient mongoClient, DbConfiguration configuration) {
    this.mongoClient = mongoClient;
    this.configuration = configuration;
  }

  /**
   * @see com.grahamcrockford.orko.submit.JobLocker#attemptLock(java.lang.String, java.util.UUID)
   */
  @Override
  public boolean attemptLock(String jobId, UUID uuid) {
    BasicDBObject doc = new BasicDBObject()
        .append("_id", jobId)
        .append("ts", Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)))
        .append("aid", uuid.toString());
    try {
      lock.get().insert(doc);
      return true;
    } catch (DuplicateKeyException e) {
      return false;
    }
  }

  /**
   * @see com.grahamcrockford.orko.submit.JobLocker#releaseLock(java.lang.String, java.util.UUID)
   */
  @Override
  public void releaseLock(String jobId, UUID uuid) {
    BasicDBObject query = new BasicDBObject()
        .append("_id", jobId)
        .append("aid", uuid.toString());
    lock.get().remove(query);
  }

  /**
   * @see com.grahamcrockford.orko.submit.JobLocker#releaseAnyLock(java.lang.String)
   */
  @Override
  public void releaseAnyLock(String jobId) {
    BasicDBObject query = new BasicDBObject()
        .append("_id", jobId);
    lock.get().remove(query);
  }

  /**
   * @see com.grahamcrockford.orko.submit.JobLocker#releaseAllLocks()
   */
  @Override
  public void releaseAllLocks() {
    lock.get().remove(new BasicDBObject());
  }

  /**
   * @see com.grahamcrockford.orko.submit.JobLocker#updateLock(java.lang.String, java.util.UUID)
   */
  @Override
  public boolean updateLock(String jobId, UUID uuid) {
    BasicDBObject query = new BasicDBObject()
        .append("_id", jobId)
        .append("aid", uuid.toString());
    BasicDBObject update = new BasicDBObject()
        .append("$set", new BasicDBObject().append("ts", Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC))));
    try {
      WriteResult result = lock.get().update(query, update);
      if (result.getN() != 1) {
        LOGGER.info("Job id " + jobId + " lost lock.");
        return false;
      }
      return true;
    } catch (Exception e) {
      LOGGER.error("Job id " + jobId + " lost lock.", e);
      return false;
    }
  }

  private DBCollection createLockCollection() {
    DBCollection exclusiveLock = mongoClient.getDB(configuration.getMongoDatabase()).getCollection("job").getCollection("lock");
    createTtlIndex(exclusiveLock);
    createAidIndex(exclusiveLock);
    return exclusiveLock;
  }

  private void createAidIndex(DBCollection exclusiveLock) {
    BasicDBObject index = new BasicDBObject();
    index.put("aid", 1);
    BasicDBObject indexOpts = new BasicDBObject();
    indexOpts.put("unique", false);
    exclusiveLock.createIndex(index, indexOpts);
  }

  private void createTtlIndex(DBCollection exclusiveLock) {
    BasicDBObject index = new BasicDBObject().append("ts", 1);
    BasicDBObject indexOpts = new BasicDBObject()
        .append("name", "ttl")
        .append("expireAfterSeconds", configuration.getLockSeconds());
    try {
      exclusiveLock.createIndex(index, indexOpts);
    } catch (MongoException e) {
      LOGGER.warn("TTL index failed to be created ({}). Dropping existing TTL indexes", e.getMessage());
      safeDropIndex(exclusiveLock, "ts_1");
      safeDropIndex(exclusiveLock, "ttl");
      exclusiveLock.createIndex(index, indexOpts);
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
}