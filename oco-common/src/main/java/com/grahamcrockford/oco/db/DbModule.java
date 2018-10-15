package com.grahamcrockford.oco.db;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.OrkoConfiguration;
import com.grahamcrockford.oco.marketdata.PermanentSubscriptionAccess;
import com.grahamcrockford.oco.submit.JobAccess;
import com.grahamcrockford.oco.submit.JobLocker;
import com.grahamcrockford.oco.util.CheckedExceptions;
import com.grahamcrockford.oco.wiring.EnvironmentInitialiser;
import com.mongodb.MongoClient;
import io.dropwizard.lifecycle.Managed;

public class DbModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(DbModule.class);

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(MongoClientTask.class);
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class).addBinding().to(DbEnvironment.class);
  }

  @Provides
  DbConfiguration dbConfiguration(OrkoConfiguration orkoConfiguration) {
    return orkoConfiguration.getDatabase();
  }

  @Provides
  @Singleton
  MongoClientTask mongoClientTask(@Nullable DbConfiguration configuration, ObjectMapper objectMapper) {
    return new MongoClientTask(configuration, objectMapper);
  }

  @Provides
  @Singleton
  MongoClient mongoClient(@Nullable DbConfiguration configuration, MongoClientTask mongoClientTask) {
    if (configuration == null)  {
      return null;
    }
    MongoClient mongoClient = null;
    while (mongoClient == null) {
      try {
        mongoClient = mongoClientTask.getMongoClient();
      } catch (Exception e) {
        LOGGER.error("Failed to create Mongo client", e);
        CheckedExceptions.runUnchecked(() -> Thread.sleep(10000));
      }
    }
    return mongoClient;
  }

  @Provides
  @Singleton
  JobAccess jobAccess(@Nullable DbConfiguration configuration, Provider<DbJobAccess> dbJobAccess, Provider<InMemoryJobAccess> inMemoryJobAccess) {
    if (configuration == null) {
      LOGGER.warn("Falling back to in-memory storage as no database is configured");
      return inMemoryJobAccess.get();
    }
    return dbJobAccess.get();
  }

  @Provides
  @Singleton
  JobLocker jobLocker(@Nullable DbConfiguration configuration, Provider<DbJobLocker> dbJobLocker, Provider<InMemoryJobAccess> inMemoryJobAccess) {
    if (configuration == null) {
      return inMemoryJobAccess.get();
    }
    return dbJobLocker.get();
  }

  @Provides
  @Singleton
  PermanentSubscriptionAccess permanentSubscriptionAccess(@Nullable DbConfiguration configuration, Provider<DbPermanentSubscriptionAccess> db, Provider<InMemoryPermanentSubscriptionAccess> inMemory) {
    if (configuration == null) {
      return inMemory.get();
    }
    return db.get();
  }
}