package com.gruelbox.orko.jobrun.spi;

/**
 * {@link Job} status.
 *
 * @author Graham Crockford
 */
public enum Status {

  /**
   * The job is currently running.
   */
  RUNNING,

  /**
   * The job ran successfully to completion.
   */
  SUCCESS,

  /**
   * The job failed permanently and should be removed.
   */
  FAILURE_PERMANENT,

  /**
   * The job failed temporarily and should be shut down
   * locally but picked up again in the future.
   */
  FAILURE_TRANSIENT

}