package com.grahamcrockford.orko.db;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import io.dropwizard.lifecycle.Managed;

@Singleton
final class MongoClientTask implements Managed {

  private final MongoClient mongoClient;

  MongoClientTask(@Nullable DbConfiguration configuration, ObjectMapper objectMapper) {
    this.mongoClient = configuration == null ? null : new MongoClient(new MongoClientURI(configuration.getMongoClientURI()));
  }

  @Override
  public void start() throws Exception {
    if (this.mongoClient == null) {
      stop();
    }
  }

  @Override
  public void stop() throws Exception {
    if (this.mongoClient != null) {
      this.mongoClient.close();
    }
  }

  MongoClient getMongoClient() {
    return mongoClient;
  }
}