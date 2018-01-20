package com.grahamcrockford.oco.orders;

import java.util.List;
import org.slf4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;

class ColumnLogger {

  private final List<LogColumn> columns;
  private final Logger logger;
  private final String logFormat;

  ColumnLogger(Logger logger, LogColumn.Builder... columns) {
    this.logger = logger;
    this.columns = FluentIterable.from(columns).transform(LogColumn.Builder::build).toList();
    this.logFormat = "| " + FluentIterable.from(this.columns)
      .transform(c -> "%" + (c.rightAligned() ? "" : "-") + c.width() + "s")
      .join(Joiner.on(" | ")) + " |";
  }

  void header() {
    final Object[] empties = FluentIterable.from(columns).transform(c -> "").toArray(String.class);
    final Object[] names = FluentIterable.from(columns).transform(LogColumn::name).toArray(String.class);
    logger.info(String.format(logFormat, empties));
    logger.info(String.format(logFormat, names));
    logger.info(String.format(logFormat, empties));
  }

  void line(Object... values) {
    logger.info(String.format(logFormat, values));
  }
}