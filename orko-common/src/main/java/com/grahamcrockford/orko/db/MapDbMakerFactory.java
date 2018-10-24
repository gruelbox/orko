package com.grahamcrockford.orko.db;

import org.mapdb.DBMaker;

import com.google.inject.Inject;
import com.grahamcrockford.orko.db.DbConfiguration.DbType;

class MapDbMakerFactory {

  private final DbType dbType;
  private final DbConfiguration dbConfiguration;

  @Inject
  MapDbMakerFactory(DbType dbType, DbConfiguration dbConfiguration) {
    this.dbType = dbType;
    this.dbConfiguration = dbConfiguration;
  }

  public DBMaker.Maker create(String name) {
    switch (dbType) {
      case MAP_DB_FILE:
        return DBMaker.fileDB(dbConfiguration.getMapDbFileDir() + "/" + name + ".db")
            .fileMmapEnable()
            .closeOnJvmShutdown()
            .concurrencyScale(4)
            .transactionEnable();
      case MAP_DB_MEMORY:
        return DBMaker.heapDB()
            .concurrencyScale(4);
      default:
        throw new IllegalStateException("MapDB not configured");
    }
  }
}