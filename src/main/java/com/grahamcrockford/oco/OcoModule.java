package com.grahamcrockford.oco;

import javax.ws.rs.client.Client;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.grahamcrockford.oco.core.CoreModule;
import com.grahamcrockford.oco.resources.ResourcesModule;

/**
 * Top level bindings.
 */
class OcoModule extends AbstractModule {

  private final ObjectMapper objectMapper;
  private final OcoConfiguration configuration;
  private final Client client;

  public OcoModule(OcoConfiguration configuration, ObjectMapper objectMapper, Client client) {
    this.configuration = configuration;
    this.objectMapper = objectMapper;
    this.client = client;
  }

  @Override
  protected void configure() {
    install(new CoreModule());
    install(new ResourcesModule());
  }

  @Provides
  DB db() {
    return DBMaker
      .fileDB("trading.db")
      .fileMmapEnable()
      .make();
  }

  @Provides
  ObjectMapper objectMapper() {
    return objectMapper;
  }

  @Provides
  OcoConfiguration config() {
    return configuration;
  }

  @Provides
  Client client() {
    return client;
  }
}