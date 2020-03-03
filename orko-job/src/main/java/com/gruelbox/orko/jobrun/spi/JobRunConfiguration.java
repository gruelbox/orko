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

/**
 * Configuration required by the job runner. Should be bound by a parent module.
 *
 * @author Graham Crockford
 */
public class JobRunConfiguration {

  private int guardianLoopSeconds;
  private int databaseLockSeconds;

  /**
   * @return The number of seconds between each attempt to start jobs and obtain locks on an
   *     instance of the application.
   */
  public int getGuardianLoopSeconds() {
    return guardianLoopSeconds;
  }

  /**
   * @param loopSeconds The number of seconds between each attempt to start jobs and obtain locks on
   *     an instance of the application.
   */
  public void setGuardianLoopSeconds(int loopSeconds) {
    guardianLoopSeconds = loopSeconds;
  }

  /**
   * @return The number of seconds that a job lock should be retained if the guardian loop doesn't
   *     refresh the lock. Should be significantly longer than {@link #getGuardianLoopSeconds()}.
   */
  public int getDatabaseLockSeconds() {
    return databaseLockSeconds;
  }

  /**
   * @param databaseLockSeconds The number of seconds that a job lock should be retained if the
   *     guardian loop doesn't refresh the lock. Should be significantly longer than {@link
   *     #getGuardianLoopSeconds()}.
   */
  public void setDatabaseLockSeconds(int databaseLockSeconds) {
    this.databaseLockSeconds = databaseLockSeconds;
  }
}
