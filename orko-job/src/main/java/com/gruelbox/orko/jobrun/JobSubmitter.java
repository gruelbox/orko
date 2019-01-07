/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gruelbox.orko.jobrun;


import com.gruelbox.orko.jobrun.spi.Job;

/**
 * Submits new jobs.
 */
public interface JobSubmitter {

  /**
   * Submits a job.
   *
   * <p>
   * {@link Job#id()} may be set or not. If set, the value passed will be retained
   * so can be used to ensure idempotency. If not set, it will be set to a random
   * value before saving.
   * </p>
   *
   * @param job The job.
   * @return The job including any generated id. Do not assume this will be the
   *         same object as first supplied.
   * @throws Exception Exception.
   */
  Job submitNew(Job job) throws Exception;

  /**
   * As {@link #submitNew(Job)} but restates any checked exceptions as
   * {@link RuntimeException}.
   *
   * <p>
   * {@link Job#id()} may be set or not. If set, the value passed will be retained
   * so can be used to ensure idempotency. If not set, it will be set to a random
   * value before saving.
   * </p>
   *
   * @param job The job.
   * @return The job including any generated id. Do not assume this will be the
   *         same object as first supplied.
   */
  default Job submitNewUnchecked(Job job) {
    try {
      return submitNew(job);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
