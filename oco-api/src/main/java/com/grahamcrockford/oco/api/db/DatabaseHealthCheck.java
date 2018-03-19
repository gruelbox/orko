package com.grahamcrockford.oco.api.db;

import org.bson.Document;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

/**
 * Just attempts to access the DB.
 *
 * @author grahamc (Graham Crockford)
 */
@Singleton
class DatabaseHealthCheck extends HealthCheck {

  private final MongoClient mongoClient;
  private final DbConfiguration dbConfiguration;


  @Inject
  DatabaseHealthCheck(MongoClient mongoClient, DbConfiguration dbConfiguration) {
    this.mongoClient = mongoClient;
    this.dbConfiguration = dbConfiguration;
  }


  @Override
  protected Result check() throws Exception {
    MongoCollection<Document> collection = mongoClient.getDatabase(dbConfiguration.getMongoDatabase()).getCollection("healthCheck");
    collection.count();
    return Result.healthy();
  }
}