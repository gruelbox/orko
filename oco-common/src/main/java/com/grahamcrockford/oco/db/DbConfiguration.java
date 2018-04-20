package com.grahamcrockford.oco.db;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DbConfiguration {

  @NotNull
  private String mongoClientURI;

  @NotNull
  private String mongoDatabase;

  @NotNull
  @Min(1L)
  private int lockSeconds;

  public DbConfiguration() {
    super();
  }

  @JsonProperty
  public String getMongoClientURI() {
    return mongoClientURI;
  }

  @JsonProperty
  public void setMongoClientURI(String mongoClientURI) {
    this.mongoClientURI = mongoClientURI;
  }

  @JsonProperty
  public String getMongoDatabase() {
    return mongoDatabase;
  }

  @JsonProperty
  public void setMongoDatabase(String mongoDatabase) {
    this.mongoDatabase = mongoDatabase;
  }

  @JsonProperty
  public int getLockSeconds() {
    return lockSeconds;
  }

  @JsonProperty
  public void setLockSeconds(int lockSeconds) {
    this.lockSeconds = lockSeconds;
  }
}