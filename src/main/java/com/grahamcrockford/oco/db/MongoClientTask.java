package com.grahamcrockford.oco.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.OcoConfiguration;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import io.dropwizard.lifecycle.Managed;

@Singleton
final class MongoClientTask implements Managed {

  private final MongoClient mongoClient;

  MongoClientTask(OcoConfiguration configuration, ObjectMapper objectMapper) {
    this.mongoClient = new MongoClient(new MongoClientURI(configuration.getMongoClientURI()));
  }

  @Override
  public void start() throws Exception {
  }

  @Override
  public void stop() throws Exception {
    this.mongoClient.close();
  }

  MongoClient getMongoClient() {
    return mongoClient;
  }
}