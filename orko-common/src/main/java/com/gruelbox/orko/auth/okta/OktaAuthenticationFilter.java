package com.gruelbox.orko.auth.okta;

import java.util.Optional;

import javax.annotation.Priority;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.auth.AuthenticatedUser;
import com.gruelbox.orko.auth.TokenAuthenticationFilter;

import io.dropwizard.auth.AuthenticationException;

/**
 * Container-level filter which performs JWT verification.  We do it this way so that it'll
 * work for Jersey, Websockets, the admin interface and any static assets. This is the lowest
 * common denominator.
 */
@Singleton
@Priority(102)
class OktaAuthenticationFilter extends TokenAuthenticationFilter {

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
}