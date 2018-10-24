package com.grahamcrockford.orko.db;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.auth.IpWhitelistAccess;
import com.grahamcrockford.orko.db.DbConfiguration.DbType;
import com.grahamcrockford.orko.marketdata.PermanentSubscriptionAccess;
import com.grahamcrockford.orko.submit.JobAccess;
import com.grahamcrockford.orko.submit.JobLocker;
import com.grahamcrockford.orko.util.CheckedExceptions;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;
import com.mongodb.MongoClient;

import io.dropwizard.lifecycle.Managed;

public class DbModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(DbModule.class);

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(MongoDbClientLifecycleTask.class);
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class).addBinding().to(DbEnvironment.class);
  }

  @Provides
  DbConfiguration dbConfiguration(OrkoConfiguration orkoConfiguration) {
    return orkoConfiguration.getDatabase();
  }

  @Provides
  DbType dbType(@Nullable DbConfiguration configuration) {
    if (configuration == null)  {
      return DbType.MAP_DB_MEMORY;
    }
    return configuration.getDbType();
  }

  @Provides
  @Singleton
  MongoClient mongoClient(@Nullable DbConfiguration configuration, DbType dbType, MongoDbClientLifecycleTask mongoDbClientLifecycleTask) {
    if (dbType != DbType.MONGO)  {
      return null;
    }
    MongoClient mongoClient = null;
    while (mongoClient == null) {
      try {
        mongoClient = mongoDbClientLifecycleTask.getMongoClient();
      } catch (Exception e) {
        LOGGER.error("Failed to create Mongo client", e);
        CheckedExceptions.runUnchecked(() -> Thread.sleep(10000));
      }
    }
    return mongoClient;
  }

  @Provides
  @Singleton
  MongoDbClientLifecycleTask mongoDbClientLifecycleTask(@Nullable DbConfiguration configuration, DbType dbType, ObjectMapper objectMapper) {
    return new MongoDbClientLifecycleTask(configuration, dbType, objectMapper);
  }

  @Provides
  @Singleton
  JobAccess jobAccess(DbType dbType, Provider<MongoJobAccess> mongo, Provider<MapDbJobAccess> mapDb) {
    if (dbType != DbType.MONGO) {
      return mapDb.get();
    }
    return mongo.get();
  }

  @Provides
  @Singleton
  JobLocker jobLocker(DbType dbType, Provider<MongoJobLocker> mongo, Provider<MapDbJobAccess> mapDb) {
    if (dbType != DbType.MONGO) {
      return mapDb.get();
    }
    return mongo.get();
  }

  @Provides
  @Singleton
  PermanentSubscriptionAccess permanentSubscriptionAccess(DbType dbType,  Provider<MongoPermanentSubscriptionAccess> mongo, Provider<MapDbPermanentSubscriptionAccess> mapDb) {
    if (dbType != DbType.MONGO) {
      return mapDb.get();
    }
    return mongo.get();
  }

  @Provides
  @Singleton
  IpWhitelistAccess ipWhitelistAccess(DbType dbType, Provider<MongoIpWhitelistAccess> mongo, Provider<MapDbIpWhitelistAccess> mapDb) {
    if (dbType != DbType.MONGO) {
      return mapDb.get();
    }
    return mongo.get();
  }
}