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
import java.util.UUID;

/**
 * Allows exclusive access to jobs to be obtained and released, allowing multiple threads or JVMs to
 * compete for the available work without double processing.
 */
@ImplementedBy(JobLockerImpl.class)
interface JobLocker {

  /**
   * Attempts to lock the job. This lock will expire after a configured period (depending on
   * implementation) so must be periodically refreshed using {@link #updateLock(String, UUID)} as
   * long as it is required.
   *
   * @param jobId The job ID.
   * @param uuid A client ID identifying the caller. Needs to be used later to release the lock.
   * @return true if the job was successfully locked. If false, this should be treated gracefully;
   *     it probably just means someone else got in first.
   */
  boolean attemptLock(String jobId, UUID uuid);

  /**
   * Updates a previously obtained lock, resetting the timeout.
   *
   * @param jobId The job id.
   * @param uuid The caller UUID.
   * @return true if the lock was successfully refreshed. If false, this is a signal to stop
   *     processing. The client has lost the lock, probably due to the job being deleted, or the
   *     lock expired and has been taken by someone else.
   */
  boolean updateLock(String jobId, UUID uuid);

  /**
   * Releases a previously obtained lock.
   *
   * @param jobId The job id.
   * @param uuid The caller UUID.
   */
  void releaseLock(String jobId, UUID uuid);

  /**
   * Releases all locks against the specified job. Use generally when the job is finished or
   * deleted.
   *
   * @param jobId The job id.
   */
  void releaseAnyLock(String jobId);

  /** Release all locks against all jobs. */
  void releaseAllLocks();
}
