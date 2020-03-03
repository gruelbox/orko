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
package com.gruelbox.orko.jobrun;

/**
 * Event fired on a regular tick. {@link JobRunner} should refresh the database lock held by a job
 * whenever this circulates.
 *
 * @author Graham Crockford
 */
final class KeepAliveEvent {

  public static final KeepAliveEvent INSTANCE = new KeepAliveEvent();

  private KeepAliveEvent() {}
}
