package com.grahamcrockford.oco.submit;

import java.util.function.Supplier;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.db.DbConfiguration;
import com.grahamcrockford.oco.spi.Job;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;


/**
 * Direct access to the data store.
 */
@Singleton
public class JobAccess {

  private final Supplier<JacksonDBCollection<Envelope, String>> collection = Suppliers.memoize(this::collection);

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
   * Inserts the job.
   *
   * @param job The job.
   * @throws JobAlreadyExistsException If the job has already been written
   */
  public void insert(Job job) throws JobAlreadyExistsException {
    try {
      collection.get().insert(Envelope.live(job));
    } catch (DuplicateKeyException e) {
      throw new JobAlreadyExistsException(e);
    }
  }

  /**
   * Updates the job.
   *
   * @param <T> The job type.
   * @param job The job.
   * @param clazz Sets the job type.
   */
  public void update(Job job) {
    collection.get().update(DBQuery.is("_id", job.id()), Envelope.live(job));
  }

  public Job load(String id) {
    Envelope envelope = collection.get().findOneById(id);
    if (envelope == null)
      throw new JobDoesNotExistException();
    if (envelope.job() == null)
      throw new JobDoesNotExistException();
    return envelope.job();
  }

  public Iterable<Job> list() {
    return FluentIterable.from(collection.get().find(DBQuery.is("processed", false))).transform(Envelope::job);
  }

  public void delete(String jobId) {
    collection.get().update(DBQuery.is("_id", jobId), Envelope.dead(jobId));
    jobLocker.releaseAnyLock(jobId);
  }

  public void delete() {
    collection.get().update(
      new BasicDBObject().append("processed", false),
      new BasicDBObject().append("job", null).append("processed", true)
    );
    jobLocker.releaseAllLocks();
  }

  private JacksonDBCollection<Envelope, String> collection() {
    DBCollection collection = mongoClient.getDB(configuration.getMongoDatabase()).getCollection("job");
    createProcessedIndex(collection);
    return JacksonDBCollection.wrap(collection, Envelope.class, String.class);
  }

  private void createProcessedIndex(DBCollection collection) {
    BasicDBObject index = new BasicDBObject();
    index.put("processed", -1);
    index.put("_id", 1);
    BasicDBObject indexOpts = new BasicDBObject();
    indexOpts.put("unique", false);
    collection.createIndex(index, indexOpts);
  }

  public static final class JobAlreadyExistsException extends Exception {

    private static final long serialVersionUID = 6959971340282376242L;

    JobAlreadyExistsException(Throwable cause) {
      super(cause);
    }
  }

  public static final class JobDoesNotExistException extends RuntimeException {

    private static final long serialVersionUID = 9086830214079119838L;

    JobDoesNotExistException() {
      super();
    }
  }
}