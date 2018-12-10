package com.gruelbox.orko.db;

import static org.hibernate.cfg.AvailableSettings.DIALECT;
import static org.hibernate.cfg.AvailableSettings.LOG_SESSION_METRICS;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.jdbc.DatabaseType;
import org.alfasoftware.morf.jdbc.SqlScriptExecutorProvider;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.testing.DatabaseSchemaManager;
import org.alfasoftware.morf.testing.DatabaseSchemaManager.TruncationBehavior;
import org.hibernate.SessionFactory;

import com.google.common.base.MoreObjects;
import com.google.inject.util.Providers;

import ch.qos.logback.classic.Level;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.junit.DAOTestRule;

public class DbTesting {

  static {
    BootstrapLogging.bootstrap(Level.INFO);
  }

  private static final ConnectionResources CONNECTION_RESOURCES;
  private static final DbConfiguration DB_CONFIGURATION = new DbConfiguration();
  private static final DatabaseSchemaManager SCHEMA_MANAGER;

  static {
    String testDbUrl = MoreObjects.firstNonNull(System.getProperty("testdb.url"), "h2:mem:test;DB_CLOSE_DELAY=-1;MVCC=TRUE;DEFAULT_LOCK_TIMEOUT=60000");
    CONNECTION_RESOURCES = DatabaseType.Registry.urlToConnectionResources("jdbc:" + testDbUrl);
    DB_CONFIGURATION.setConnectionString(testDbUrl);
    SCHEMA_MANAGER = new DatabaseSchemaManager(CONNECTION_RESOURCES, CONNECTION_RESOURCES.getDataSource(), new SqlScriptExecutorProvider(CONNECTION_RESOURCES)) {};
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

  public static DAOTestRule.Builder rule() {
    return DAOTestRule.newBuilder()
      .setUrl(DB_CONFIGURATION.getJdbcUrl())
      .setDriver(DB_CONFIGURATION.getDriver())
      .setProperty("charset", "UTF-8")
      .setProperty(LOG_SESSION_METRICS, "false")
      .setProperty(DIALECT, DialectResolver.hibernateDialect(CONNECTION_RESOURCES.getDatabaseType()))
      .setUsername("");
  }
}