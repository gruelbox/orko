package com.grahamcrockford.orko.db;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DbConfiguration {

  /**
   * Mongo client URI.
   */
  @NotNull
  private String mongoClientURI;

  /**
   * The name of the mongo database.
   */
  @NotNull
  private String mongoDatabase;

  /**
   * How long database locks should persist for, in seconds.  Too short and
   * you'll end up workers fighting over the same jobs.  Too long
   * and if instances go down, it'll take ages for other instances to pick
   * up their jobs.
   */
  @NotNull
  @Min(10L)
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