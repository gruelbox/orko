package com.grahamcrockford.orko.db;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.jdbc.DatabaseType;
import org.alfasoftware.morf.jdbc.SqlScriptExecutorProvider;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.testing.DatabaseSchemaManager;
import org.alfasoftware.morf.testing.DatabaseSchemaManager.TruncationBehavior;

import com.google.inject.util.Providers;

import jersey.repackaged.com.google.common.base.MoreObjects;

public class DbTesting {
  
  // TODO set up from environment variables
  private static final ConnectionResources CONNECTION_RESOURCES;
  private static final DbConfiguration DB_CONFIGURATION = new DbConfiguration();
  static {
    String testDbUrl = MoreObjects.firstNonNull(System.getProperty("testdb.url"), "h2:mem:test;DB_CLOSE_DELAY=-1;MVCC=TRUE;DEFAULT_LOCK_TIMEOUT=60000");
    CONNECTION_RESOURCES = DatabaseType.Registry.urlToConnectionResources("jdbc:" + testDbUrl);
    DB_CONFIGURATION.setConnectionString(testDbUrl);
  };
  private static final ConnectionSource CONNECTION_SOURCE = new ConnectionSource(Providers.of(DB_CONFIGURATION), Providers.of(CONNECTION_RESOURCES));
  
  public static ConnectionSource connectionSource() {
    return CONNECTION_SOURCE;
  }
  
  public static DbConfiguration dbConfiguration() {
    return DB_CONFIGURATION;
  }
  
  public static ConnectionResources connectionResources() {
    return CONNECTION_RESOURCES;
  }
  
  public static void mutateToSupportSchema(Schema schema) {
    new DatabaseSchemaManager(CONNECTION_RESOURCES, CONNECTION_RESOURCES.getDataSource(), new SqlScriptExecutorProvider(CONNECTION_RESOURCES)) {
    }.mutateToSupportSchema(schema, TruncationBehavior.ALWAYS);
  }
}