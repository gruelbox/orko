package com.grahamcrockford.oco.api.process;

import org.bson.types.ObjectId;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.db.DbConfiguration;
import com.grahamcrockford.oco.spi.Job;
import com.mongodb.BasicDBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;


/**
 * Simple wrapper for sending orders to the queue.
 */
@Singleton
public class JobAccess {

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
      collection(Envelope.class).insert(Envelope.live(job));
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
    collection(Envelope.class).update(DBQuery.is("_id", job.id()), Envelope.live(job));
  }

  public Job load(String id) {
    Job job = collection(Envelope.class).findOneById(new ObjectId(id)).job();
    if (job == null)
      throw new JobDoesNotExistException();
    return job;
  }

  public Iterable<Job> list() {
    return
      FluentIterable.from(
          collection(Envelope.class).find(DBQuery.is("processed", false))
      ).transform(Envelope::job);
  }

  public void delete(String jobId) {
    collection(Envelope.class).update(DBQuery.is("_id", jobId), Envelope.dead(jobId));
  }


  public void delete() {
    collection(Envelope.class).update(
      new BasicDBObject().append("processed", false),
      new BasicDBObject().append("job", null).append("processed", true)
    );
    jobLocker.releaseAllLocks();
  }

  private <T> JacksonDBCollection<T, ObjectId> collection(Class<T> clazz) {
    return JacksonDBCollection.wrap(mongoClient.getDB(configuration.getMongoDatabase()).getCollection("job"), clazz, org.bson.types.ObjectId.class);
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