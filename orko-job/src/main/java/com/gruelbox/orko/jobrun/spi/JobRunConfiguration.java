package com.gruelbox.orko.jobrun.spi;

/**
 * Configuration required by the job runner. Should be
 * bound by a parent module.
 *
 * @author Graham Crockford
 */
public class JobRunConfiguration {

  private int guardianLoopSeconds;
  private int databaseLockSeconds;

  /**
   * @return The number of seconds between each attempt to start jobs and obtain
   *         locks on an instance of the application.
   */
  public int getGuardianLoopSeconds() {
    return guardianLoopSeconds;
  }

  /**
   * @param loopSeconds The number of seconds between each attempt to start jobs
   *                    and obtain locks on an instance of the application.
   */
  public void setGuardianLoopSeconds(int loopSeconds) {
    guardianLoopSeconds = loopSeconds;
  }

  /**
   * @return The number of seconds that a job lock should be retained if the
   *         guardian loop doesn't refresh the lock. Should be significantly
   *         longer than {@link #getGuardianLoopSeconds()}.
   */
  public int getDatabaseLockSeconds() {
    return databaseLockSeconds;
  }

  /**
   * @param databaseLockSeconds The number of seconds that a job lock should be
   *                            retained if the guardian loop doesn't refresh the
   *                            lock. Should be significantly longer than
   *                            {@link #getGuardianLoopSeconds()}.
   */
  public void setDatabaseLockSeconds(int databaseLockSeconds) {
    this.databaseLockSeconds = databaseLockSeconds;
  }
}
