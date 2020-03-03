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
 * Passed to a {@link Job} on startup, allowing it to asynchronously update itself or finish.
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
   * A job should call this once it has finished processing to ensure it is shut down and removed.
   * It will be called back using {@link JobProcessor#stop()} to clear its resources.
   *
   * @param status The completion state.
   */
  public void finish(Status status);
}
