/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.jobrun;

import com.google.inject.ImplementedBy;
import com.gruelbox.orko.jobrun.spi.Job;

/** Allows CRUD access to jobs. */
@ImplementedBy(JobAccessImpl.class)
interface JobAccess {

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
   * @param job The job.
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

  /** Deletes all jobs. */
  void deleteAll();

  /**
   * Thrown on attempting to insert a job that has been created before (even if it no longer
   * exists).
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

  /** Thrown on attempting to access a job that does not exist. */
  public static final class JobDoesNotExistException extends RuntimeException {

    private static final long serialVersionUID = 9086830214079119838L;

    public JobDoesNotExistException() {
      super();
    }
  }
}
