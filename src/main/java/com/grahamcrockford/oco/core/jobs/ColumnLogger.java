package com.grahamcrockford.oco.core.jobs;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;

class ColumnLogger {

  private final List<LogColumn> columns;
  private final Logger logger;
  private final String logFormat;

  private final AtomicInteger logRowCount = new AtomicInteger();

  ColumnLogger(Logger logger, LogColumn.Builder... columns) {
    this.logger = logger;
    this.columns = FluentIterable.from(columns).transform(LogColumn.Builder::build).toList();
    this.logFormat = "| " + FluentIterable.from(this.columns)
      .transform(c -> "%" + (c.rightAligned() ? "" : "-") + c.width() + "s")
      .join(Joiner.on(" | ")) + " |";
  }

  private void header() {
    final Object[] empties = FluentIterable.from(columns).transform(c -> "").toArray(String.class);
    final Object[] names = FluentIterable.from(columns).transform(LogColumn::name).toArray(String.class);
    logger.info(String.format(logFormat, empties));
    logger.info(String.format(logFormat, names));
    logger.info(String.format(logFormat, empties));
  }

  void line(Object... values) {
    final int rowCount = logRowCount.getAndIncrement();
    if (rowCount == 0) {
      header();
    }
    if (rowCount == 25) {
      logRowCount.set(0);
    }
    logger.info(String.format(logFormat, values));
  }
}