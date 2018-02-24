package com.grahamcrockford.oco.auth;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.basic.BasicCredentials;

@Singleton
public class SimpleAuthenticator implements Authenticator<BasicCredentials, User>, Authorizer<User> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAuthenticator.class);

  private final AuthDb authDb;
  private final IpWhitelisting ipWhitelisting;
  private final Provider<HttpServletRequest> requestProvider;

  @Inject
  SimpleAuthenticator(AuthDb authDb, IpWhitelisting ipWhitelisting, Provider<HttpServletRequest> requestProvider) {
    this.authDb = authDb;
    this.ipWhitelisting = ipWhitelisting;
    this.requestProvider = requestProvider;
  }

  @Override
  public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {

    Optional<User> user = authDb.loadUser(credentials.getUsername());

    if (!ipWhitelisting.authoriseIp()) {
      LOGGER.error("Non-whitelisted access attempt from ip: " + requestProvider.get().getRemoteAddr());
      return Optional.empty();
    }

    if (!user.isPresent()) {
      LOGGER.error("No user provided for access attempt from: " + requestProvider.get().getRemoteAddr());
      return Optional.empty();
    }

    if (!user.get().getPassword().equals(credentials.getPassword())) {
      LOGGER.error("Password mismatch for access attempt from: " + requestProvider.get().getRemoteAddr());
      return Optional.empty();
    }

    return user;
  }

  @Override
  public boolean authorize(User principal, String role) {
    return principal.isUserInRole(role);
  }
}