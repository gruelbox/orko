package com.gruelbox.orko.db;

import static org.jooq.impl.DSL.val;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Just attempts to access the DB.
 *
 * @author grahamc (Graham Crockford)
 */
@Singleton
class DatabaseHealthCheck extends HealthCheck {

  private final ConnectionSource connectionSource;

  @Inject
  DatabaseHealthCheck(ConnectionSource connectionSource) {
    this.connectionSource = connectionSource;
  }

  @Override
  protected Result check() throws Exception {
    return connectionSource.getWithNewConnection(dsl -> {
      Integer value = dsl.select(val(1)).fetchSingle(0, Integer.class);
      return value == 1 ? Result.healthy() : Result.unhealthy("Whut");
    });
  }
}