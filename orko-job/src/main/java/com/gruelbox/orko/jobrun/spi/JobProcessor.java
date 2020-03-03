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
package com.gruelbox.orko.jobrun.spi;

import com.google.inject.Injector;

/**
 * The processing code for a {@link Job}.
 *
 * @author Graham Crockford
 * @param <T> The job type.
 */
public interface JobProcessor<T extends Job> {

  /**
   * Called when a job starts. In the event of the application being shut down and the job killed,
   * this may get called again to start the job the next time.
   *
   * @return {@link Status#RUNNING} or {@link Status#FAILURE_TRANSIENT} to stay resident, {@link
   *     Status#SUCCESS} or {@link Status#FAILURE_TRANSIENT} to finish (the latter two will cause a
   *     call to {@link #stop()}).
   */
  public Status start();

  /**
   * Called to terminate a job. This may occur due to a shutdown, a loss of the processing lock, or
   * explicitly after a job calls {@link JobControl#finish(Status)}.
   */
  public default void stop() {
    // default no--op
  }

  /**
   * Called following a call to {@link JobControl#replace(Job)} to update the job itself. While the
   * job could handle this itself, this acts as a reminder to handle this state appropriately,
   * modifying callbacks and subscriptions if necessary.
   *
   * @param job The replacement job.
   */
  public void setReplacedJob(T job);

  /**
   * A factory for {@link JobProcessor}s.
   *
   * @author Graham Crockford
   * @param <T> The job type.
   */
  public interface Factory<T extends Job> {
    public JobProcessor<T> create(T job, JobControl jobControl);
  }

  /**
   * Factory method for {@link JobProcessor}s.
   *
   * @param job The job to create a processor for.
   * @param jobControl The {@link JobControl} to pass to the processor.
   * @param injector The injector to create the required {@link JobProcessor.Factory}.
   * @return The {@link JobProcessor}.
   */
  @SuppressWarnings("unchecked")
  public static JobProcessor<Job> createProcessor(
      Job job, JobControl jobControl, Injector injector) {
    return ((JobProcessor.Factory<Job>) injector.getInstance(job.processorFactory()))
        .create(job, jobControl);
  }
}
