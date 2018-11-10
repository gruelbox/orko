package com.grahamcrockford.orko.auth.okta;

import java.util.Optional;

import javax.annotation.Priority;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.auth.AuthenticatedUser;
import com.grahamcrockford.orko.auth.TokenAuthenticationFilter;

import io.dropwizard.auth.AuthenticationException;

/**
 * Container-level filter which performs JWT verification.  We do it this way so that it'll
 * work for Jersey, Websockets, the admin interface and any static assets. This is the lowest
 * common denominator.
 */
@Singleton
@Priority(102)
class OktaAuthenticationFilter extends TokenAuthenticationFilter<AuthenticatedUser> {

  private final OktaAuthenticator authenticator;

  @Inject
  OktaAuthenticationFilter(OktaAuthenticator authenticator) {
    this.authenticator = authenticator;
  }

  @Override
  protected Optional<AuthenticatedUser> extractPrincipal(String token) {
    try {
      return authenticator.authenticate(token);
    } catch (AuthenticationException e) {
      return Optional.empty();
    }
  }

  @Override
  protected boolean authorize(AuthenticatedUser principal, String role) {
    return true;
  }
}