package com.gruelbox.orko.spi;

import com.gruelbox.orko.notification.Status;

public interface JobProcessor<T extends Job> {

  /**
   * @return {@link Status#RUNNING} or {@link Status#FAILURE_TRANSIENT} to stay resident,
   * {@link Status#SUCCESS} or {@link Status#FAILURE_TRANSIENT} to finish (the latter two
   * will cause a call to {@link #stop()}).
   */
  public Status start();

  public default void stop() {
    // default no--op
  }

  public interface Factory<T extends Job> {
    public JobProcessor<T> create(T job, JobControl jobControl);
  }
}