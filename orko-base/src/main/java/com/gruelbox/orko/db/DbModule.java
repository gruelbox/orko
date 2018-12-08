package com.gruelbox.orko.db;

import com.google.inject.AbstractModule;

public class DbModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new DatabaseAccessModule());
  }
}