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
package com.gruelbox.orko.job;

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;

class ColumnLogger {

  private final List<LogColumn> columns;
  private final Logger logger;
  private final String logFormat;

  private final AtomicInteger logRowCount = new AtomicInteger();

  ColumnLogger(Logger logger, LogColumn.Builder... columns) {
    this.logger = logger;
    this.columns = FluentIterable.from(columns).transform(LogColumn.Builder::build).toList();
    this.logFormat =
        "| "
            + FluentIterable.from(this.columns)
                .transform(c -> "%" + (c.rightAligned() ? "" : "-") + c.width() + "s")
                .join(Joiner.on(" | "))
            + " |";
  }

  private void header() {
    if (logger.isDebugEnabled()) {
      final Object[] empties =
          FluentIterable.from(columns).transform(c -> "").toArray(String.class);
      final Object[] names =
          FluentIterable.from(columns).transform(LogColumn::name).toArray(String.class);
      logger.debug(String.format(logFormat, empties));
      logger.debug(String.format(logFormat, names));
      logger.debug(String.format(logFormat, empties));
    }
  }

  void line(Object... values) {
    if (logger.isDebugEnabled()) {
      final int rowCount = logRowCount.getAndIncrement();
      if (rowCount == 0) {
        header();
      }
      if (rowCount == 25) {
        logRowCount.set(0);
      }
      logger.debug(String.format(logFormat, values));
    }
  }
}
