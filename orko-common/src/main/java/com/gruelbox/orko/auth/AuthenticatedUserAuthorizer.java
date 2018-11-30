package com.gruelbox.orko.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.auth.Authorizer;

public class AuthenticatedUserAuthorizer implements Authorizer<AuthenticatedUser> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatedUserAuthorizer.class);

  @Override
  public boolean authorize(AuthenticatedUser authenticatedUser, String role) {
    if (authenticatedUser == null) {
      LOGGER.warn("No user provided for authorization");
      return false;
    }
    return authenticatedUser.getRoles().contains(role);
  }

}
