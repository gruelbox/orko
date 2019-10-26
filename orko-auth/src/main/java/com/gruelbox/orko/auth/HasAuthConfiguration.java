package com.gruelbox.orko.auth;

/**
 * Interface to be supported by any application configuration which supports authentication
 */
public interface HasAuthConfiguration {

  /**
   * @return Authentication configuration.
   */
  AuthConfiguration getAuth();

}
