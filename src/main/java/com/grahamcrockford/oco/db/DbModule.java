package com.grahamcrockford.oco.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.util.CheckedExceptions;
import com.mongodb.MongoClient;
import io.dropwizard.lifecycle.Managed;

public class DbModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(DbModule.class);

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(MongoClientTask.class);
  }

  @Provides
  @Singleton
  MongoClientTask mongoClientTask(OcoConfiguration configuration, ObjectMapper objectMapper) {
    return new MongoClientTask(configuration, objectMapper);
  }

  @Provides
  @Singleton
  MongoClient mongoClient(MongoClientTask mongoClientTask) {
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
}