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
 * May be supported by a {@link JobProcessor} if it is capable of updating an unstarted {@link Job}
 * to correspond with current state. Used where a job itself contains a number of other optional
 * jobs which it may start at any time.
 *
 * @author Graham Crockford
 */
public interface Validatable {

  /** Modify a job to fit current state. */
  void validate();
}
