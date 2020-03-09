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
package com.gruelbox.orko.db;

import static org.hibernate.cfg.AvailableSettings.DIALECT;
import static org.hibernate.cfg.AvailableSettings.LOG_SESSION_METRICS;

import ch.qos.logback.classic.Level;
import com.google.common.base.MoreObjects;
import com.google.inject.util.Providers;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.junit5.DAOTestExtension;
import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.jdbc.DatabaseType;
import org.alfasoftware.morf.jdbc.SqlScriptExecutorProvider;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.testing.DatabaseSchemaManager;
import org.alfasoftware.morf.testing.DatabaseSchemaManager.TruncationBehavior;
import org.hibernate.SessionFactory;

public final class DbTesting {

  static {
    BootstrapLogging.bootstrap(Level.INFO);
  }

  private DbTesting() {}

  private static final ConnectionResources CONNECTION_RESOURCES;
  private static final DbConfiguration DB_CONFIGURATION = new DbConfiguration();
  private static final DatabaseSchemaManager SCHEMA_MANAGER;

  static {
    String testDbUrl =
        MoreObjects.firstNonNull(
            System.getProperty("testdb.url"),
            "h2:mem:test;DB_CLOSE_DELAY=-1;DEFAULT_LOCK_TIMEOUT=60000");
    CONNECTION_RESOURCES = DatabaseType.Registry.urlToConnectionResources("jdbc:" + testDbUrl);
    DB_CONFIGURATION.setConnectionString(testDbUrl);
    SCHEMA_MANAGER =
        new DatabaseSchemaManager(
            CONNECTION_RESOURCES,
            CONNECTION_RESOURCES.getDataSource(),
            new SqlScriptExecutorProvider(CONNECTION_RESOURCES)) {};
  }

  public static ConnectionSource connectionSource(SessionFactory sessionFactory) {
    return new ConnectionSource(Providers.of(sessionFactory), Providers.of(CONNECTION_RESOURCES));
  }

  public static DbConfiguration dbConfiguration() {
    return DB_CONFIGURATION;
  }

  public static ConnectionResources connectionResources() {
    return CONNECTION_RESOURCES;
  }

  public static void clearDatabase() {
    SCHEMA_MANAGER.dropAllTables();
    SCHEMA_MANAGER.dropAllViews();
  }

  public static void mutateToSupportSchema(Schema schema) {
    SCHEMA_MANAGER.mutateToSupportSchema(schema, TruncationBehavior.ALWAYS);
  }

  public static void invalidateSchemaCache() {
    SCHEMA_MANAGER.invalidateCache();
  }

  public static DAOTestExtension.Builder extension() {
    return DAOTestExtension.newBuilder()
        .setUrl(DB_CONFIGURATION.getJdbcUrl())
        .setDriver(DB_CONFIGURATION.getDriver())
        .setProperty("charset", "UTF-8")
        .setProperty(LOG_SESSION_METRICS, "false")
        .setProperty(
            DIALECT, DialectResolver.hibernateDialect(CONNECTION_RESOURCES.getDatabaseType()))
        .setUsername("");
  }
}
