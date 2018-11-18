package com.grahamcrockford.orko.db;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.jdbc.ConnectionResourcesBean;
import org.alfasoftware.morf.jdbc.SqlScriptExecutorProvider;
import org.alfasoftware.morf.jdbc.h2.H2;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.testing.DatabaseSchemaManager;
import org.alfasoftware.morf.testing.DatabaseSchemaManager.TruncationBehavior;

import com.google.inject.util.Providers;

public class DbTesting {
  
  // TODO set up from environment variables
  private static final ConnectionResourcesBean CONNECTION_RESOURCES = new ConnectionResourcesBean();
  private static final DbConfiguration DB_CONFIGURATION = new DbConfiguration();
  static {
    CONNECTION_RESOURCES.setDatabaseName("test");
    CONNECTION_RESOURCES.setDatabaseType(H2.IDENTIFIER);
    DB_CONFIGURATION.setJdbcUrl("jdbc:h2:mem:test");
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