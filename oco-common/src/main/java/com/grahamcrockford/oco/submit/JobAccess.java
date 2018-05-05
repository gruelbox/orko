package com.grahamcrockford.oco.submit;

import com.grahamcrockford.oco.spi.Job;

/**
 * Allows CRUD access to jobs.
 */
public interface JobAccess {

  /**
   * Inserts the job.
   *
   * @param job The job.
   * @throws JobAlreadyExistsException If the job has already been written
   */
  void insert(Job job) throws JobAlreadyExistsException;

  /**
   * Updates the job.
   *
   * @param <T> The job type.
   * @param job The job.
   * @param clazz Sets the job type.
   */
  void update(Job job);

  /**
   * Loads the specified job.
   *
   * @param id The job id.
   * @return The job.
   */
  Job load(String id);

  /**
   * Lists the open jobs.
   *
   * @return The open jobs.
   */
  Iterable<Job> list();

  /**
   * Deletes a job.
   *
   * @param jobId The job.
   */
  void delete(String jobId);

  /**
   * Deletes all jobs.
   */
  void deleteAll();


  /**
   * Thrown on attempting to insert a job that has been created before (even if
   * it no longer exists).
   */
  public static final class JobAlreadyExistsException extends Exception {

    private static final long serialVersionUID = 6959971340282376242L;

    public JobAlreadyExistsException() {
      super();
    }

    public JobAlreadyExistsException(Throwable cause) {
      super(cause);
    }
  }

  /**
   * Thrown on attempting to access a job that does not exist.
   */
  public static final class JobDoesNotExistException extends RuntimeException {

    private static final long serialVersionUID = 9086830214079119838L;

    public JobDoesNotExistException() {
      super();
    }
  }
}