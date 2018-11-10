package com.grahamcrockford.orko.auth.jwt;

import java.util.Optional;

import javax.annotation.Priority;
import org.jose4j.jwt.consumer.JwtContext;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.auth.AuthenticatedUser;
import com.grahamcrockford.orko.auth.TokenAuthenticationFilter;

/**
 * Container-level filter which performs JWT verification. We do it this way so
 * that it'll work for Jersey, Websockets, the admin interface and any static
 * assets. This is the lowest common denominator.
 */
@Singleton
@Priority(102)
class JwtAuthenticationFilter extends TokenAuthenticationFilter<AuthenticatedUser> {

  private final JwtAuthenticatorAuthorizer authenticator;
  private final Provider<Optional<JwtContext>> jwtContext;

  @Inject
  JwtAuthenticationFilter(JwtAuthenticatorAuthorizer authenticator, Provider<Optional<JwtContext>> jwtContext) {
    this.authenticator = authenticator;
    this.jwtContext = jwtContext;
  }


  @Override
  protected Optional<AuthenticatedUser> extractPrincipal(String token) {
    return jwtContext.get().map(context -> authenticator.authenticate(context).orElse(null));
  }

  @Override
  protected boolean authorize(AuthenticatedUser principal, String role) {
    return authenticator.authorize(principal, role);
  }
}