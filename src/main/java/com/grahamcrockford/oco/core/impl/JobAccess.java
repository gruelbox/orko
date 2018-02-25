package com.grahamcrockford.oco.core.impl;

import org.bson.types.ObjectId;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.core.spi.Job;
import com.grahamcrockford.oco.db.DbConfiguration;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;


/**
 * Simple wrapper for sending orders to the queue.
 */
@Singleton
class JobAccess {

  private final MongoClient mongoClient;
  private final JobLocker jobLocker;
  private final DbConfiguration configuration;

  @Inject
  JobAccess(MongoClient mongoClient, JobLocker jobLocker, DbConfiguration configuration) {
    this.mongoClient = mongoClient;
    this.jobLocker = jobLocker;
    this.configuration = configuration;
  }

  /**
   * Enqueues the job for immediate action.
   *
   * @param <T> The job type.
   * @param job The job.
   * @return The updated order with ID set.
   */
  @SuppressWarnings("unchecked")
  public <T extends Job> T insert(T job) {
    return insert(job, (Class<T>)job.getClass());
  }

  /**
   * Enqueues the job for immediate action.
   *
   * @param <T> The job type.
   * @param job The job.
   * @param clazz Sets the job type.
   * @return The updated order with ID set.
   */
  @SuppressWarnings("unchecked")
  public <T extends Job> T insert(T job, Class<T> clazz) {
    JacksonDBCollection<T, org.bson.types.ObjectId> coll = collection(clazz);
    WriteResult<T, org.bson.types.ObjectId> result = coll.insert(job);
    org.bson.types.ObjectId savedId = result.getSavedId();
    return (T) job.toBuilder().id(savedId.toHexString()).build();
  }

  /**
   * Updates the job.
   *
   * @param <T> The job type.
   * @param job The job.
   * @param clazz Sets the job type.
   */
  public <T extends Job> void update(T job, Class<T> clazz) {
    JacksonDBCollection<T, org.bson.types.ObjectId> coll = collection(clazz);
    coll.update(DBQuery.is("_id", job.id()), job);
  }

  @SuppressWarnings("unchecked")
  public <T extends Job> T load(String id) {
    JacksonDBCollection<Job, org.bson.types.ObjectId> coll = collection(Job.class);
    return (T) coll.findOneById(new ObjectId(id));
  }

  public Iterable<Job> list() {
    JacksonDBCollection<Job, org.bson.types.ObjectId> coll = collection(Job.class);
    return coll.find(new BasicDBObject());
  }

  public void delete(String orderId) {
    collection(Job.class).removeById(new ObjectId(orderId));
    jobLocker.releaseAnyLock(orderId);
  }


  public void delete() {
    collection(Job.class).remove(new BasicDBObject());
    jobLocker.releaseAllLocks();
  }

  private <T extends Job> JacksonDBCollection<T, ObjectId> collection(Class<T> clazz) {
    return JacksonDBCollection.wrap(mongoClient.getDB(configuration.getMongoDatabase()).getCollection("job"), clazz, org.bson.types.ObjectId.class);
  }
}