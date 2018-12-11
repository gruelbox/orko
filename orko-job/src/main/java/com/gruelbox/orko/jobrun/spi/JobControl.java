package com.gruelbox.orko.jobrun.spi;

/**
 * Passed to a {@link Job} on startup, allowing it to asynchronously update
 * itself or finish.
 *
 * @author Graham Crockford
 */
public interface JobControl {

  /**
   * A job should call this if it wishes to update itself.
   *
   * @param job The updated version.
   */
  public void replace(Job job);

  /**
   * A job should call this once it has finished processing to ensure it
   * is shut down and removed. It will be called back using
   * {@link JobProcessor#stop()} to clear its resources.
   *
   * @param status The completion state.
   */
  public void finish(Status status);

}
