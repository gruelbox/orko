package com.gruelbox.orko.jobrun.spi;

/**
 * The processing code for a {@link Job}.
 *
 * @author Graham Crockford
 * @param <T> The job type.
 */
public interface JobProcessor<T extends Job> {

  /**
   * Called when a job starts. In the event of the application being shut down and
   * the job killed, this may get called again to start the job the next time.
   *
   * @return {@link Status#RUNNING} or {@link Status#FAILURE_TRANSIENT} to stay
   *         resident, {@link Status#SUCCESS} or {@link Status#FAILURE_TRANSIENT}
   *         to finish (the latter two will cause a call to {@link #stop()}).
   */
  public Status start();

  /**
   * Called to terminate a job.  This may occur due to a shutdown, a loss of the
   * processing lock, or explicitly after a job calls
   * {@link JobControl#finish(Status)}.
   */
  public default void stop() {
    // default no--op
  }

  /**
   * A factory for {@link JobProcessor}s.
   *
   * @author Graham Crockford
   * @param <T> The job type.
   */
  public interface Factory<T extends Job> {
    public JobProcessor<T> create(T job, JobControl jobControl);
  }
}