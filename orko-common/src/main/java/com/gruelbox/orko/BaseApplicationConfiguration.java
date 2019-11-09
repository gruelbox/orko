package com.gruelbox.orko;

/**
 * Configuration common to all {@link BaseApplication}s.
 */
public interface BaseApplicationConfiguration {

  /**
   * @return true if the application has been started as a child process (usually as part of an
   * integration test) and should die when the parent process dies.
   */
  boolean isChildProcess();

}
