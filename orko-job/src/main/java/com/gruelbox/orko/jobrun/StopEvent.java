package com.gruelbox.orko.jobrun;

/**
 * Event fired to tell any active {@link JobRunner}s to
 * stop their jobs and prepare for shutdown.
 *
 * @author Graham
 *
 */
final class StopEvent {

  public static final StopEvent INSTANCE = new StopEvent();

  private StopEvent() {}

}
