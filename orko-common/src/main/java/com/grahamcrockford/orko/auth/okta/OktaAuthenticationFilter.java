package com.grahamcrockford.orko.auth.okta;

import java.util.Optional;

import javax.annotation.Priority;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.auth.TokenAuthenticationFilter;

import io.dropwizard.auth.AuthenticationException;

/**
 * Container-level filter which performs JWT verification.  We do it this way so that it'll
 * work for Jersey, Websockets, the admin interface and any static assets. This is the lowest
 * common denominator.
 */
@Singleton
@Priority(102)
class OktaAuthenticationFilter extends TokenAuthenticationFilter<AccessTokenPrincipal> {

  private final OrkoAuthenticator authenticator;
  private final OrkoAuthorizer authorizer;


  @Inject
  OktaAuthenticationFilter(OrkoAuthenticator authenticator, OrkoAuthorizer authorizer) {
    this.authenticator = authenticator;
    this.authorizer = authorizer;
  }

  @Override
  protected Optional<AccessTokenPrincipal> extractPrincipal(String token) throws TokenValidationException {
    try {
      return authenticator.authenticate(token);
    } catch (AuthenticationException e) {
      throw new TokenValidationException(e);
    }
  }

  @Override
  protected boolean authorize(AccessTokenPrincipal principal, String role) {
    return authorizer.authorize(principal, role);
  }
}