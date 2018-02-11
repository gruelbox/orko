package com.grahamcrockford.oco.auth;

import java.util.Optional;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.basic.BasicCredentials;

@Singleton
public class SimpleAuthenticator implements Authenticator<BasicCredentials, User>, Authorizer<User> {

  private final AuthDb authDb;

  @Inject
  SimpleAuthenticator(AuthDb authDb) {
    this.authDb = authDb;
  }

  @Override
  public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {

    Optional<User> user = authDb.loadUser(credentials.getUsername());

    if (!user.isPresent()) {
      throw new AuthenticationException("Not authenticated");
    }

    if (!user.get().getPassword().equals(credentials.getPassword())) {
      throw new AuthenticationException("Not authenticated");
    }

    return user;
  }

  @Override
  public boolean authorize(User principal, String role) {
    return principal.isUserInRole(role);
  }
}