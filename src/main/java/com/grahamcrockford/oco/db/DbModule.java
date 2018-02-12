package com.grahamcrockford.oco.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.db.MongoClientTask;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import io.dropwizard.lifecycle.Managed;

public class DbModule extends AbstractModule {

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
    return mongoClientTask.getMongoClient();
  }

  @Provides
  @Singleton
  MongoDatabase mongoDatabase(MongoClient mongoClient, OcoConfiguration configuration) {
    return mongoClient.getDatabase(configuration.getMongoDatabase());
  }

  @Provides
  @Singleton
  DB MongoDB(MongoClient mongoClient, OcoConfiguration configuration) {
    return mongoClient.getDB(configuration.getMongoDatabase());
  }
}