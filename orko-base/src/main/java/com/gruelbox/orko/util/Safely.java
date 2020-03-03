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
package com.gruelbox.orko.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Safely {

  private Safely() {
    // Not instantiatable
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(Safely.class);

  public static boolean run(String gerund, CheckedExceptions.ThrowingRunnable runnable) {
    try {
      runnable.run();
      return true;
    } catch (Exception e) {
      LOGGER.error("Error when {}", gerund, e);
      return false;
    }
  }
}
