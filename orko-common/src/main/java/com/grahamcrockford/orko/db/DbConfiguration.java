package com.grahamcrockford.orko.db;

import java.net.URI;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DbConfiguration {

  /**
   * Mongo client URI.
   */
  @NotNull
  private String mongoClientURI;

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

  @JsonIgnore
  public String getMongoDatabase() {
    if (StringUtils.isEmpty(mongoClientURI))
      return null;
    URI uri = URI.create(mongoClientURI);
    if (!uri.getScheme().equals("mongodb"))
      throw new IllegalArgumentException("Unsupported mongodb client URI: " + mongoClientURI);
    if (StringUtils.isEmpty(uri.getPath()) || uri.getPath().length() < 2)
      throw new IllegalArgumentException("Unsupported mongodb client URI: " + mongoClientURI);
    return uri.getPath().substring(1);
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