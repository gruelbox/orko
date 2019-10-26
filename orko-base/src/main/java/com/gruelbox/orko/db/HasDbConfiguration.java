package com.gruelbox.orko.db;

/**
 * Configuration to be supported by any application which requires the database.
 */
public interface HasDbConfiguration {

  /**
   * @return Database configuration.
   */
  DbConfiguration getDatabase();

}
