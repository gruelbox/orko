package com.gruelbox.orko.jobrun;

/**
 * Event fired on a regular tick. {@link JobRunner} should
 * refresh the database lock held by a job whenever
 * this circulates.
 *
 * @author Graham Crockford
 */
final class KeepAliveEvent {

  public static final KeepAliveEvent INSTANCE = new KeepAliveEvent();

  private KeepAliveEvent() {}

}
