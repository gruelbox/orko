package com.grahamcrockford.orko.auth.jwt;

import java.util.Optional;

import javax.annotation.Priority;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtContext;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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
  private final JwtConsumer jwtConsumer;

  @Inject
  JwtAuthenticationFilter(JwtAuthenticatorAuthorizer authenticator, JwtConsumer jwtConsumer) {
    this.authenticator = authenticator;
    this.jwtConsumer = jwtConsumer;
  }


  @Override
  protected Optional<AuthenticatedUser> extractPrincipal(String token) throws TokenValidationException {
    try {
      JwtContext jwtContext = jwtConsumer.process(token);
      return authenticator.authenticate(jwtContext);
    } catch (InvalidJwtException e) {
      throw new TokenValidationException(e);
    }
  }

  @Override
  protected boolean authorize(AuthenticatedUser principal, String role) {
    return authenticator.authorize(principal, role);
  }
}