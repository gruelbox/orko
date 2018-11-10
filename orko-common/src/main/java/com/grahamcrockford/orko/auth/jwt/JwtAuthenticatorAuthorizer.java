package com.grahamcrockford.orko.auth.jwt;

import java.util.Optional;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.grahamcrockford.orko.auth.AuthenticatedUser;

import io.dropwizard.auth.Authenticator;

@Singleton
class JwtAuthenticatorAuthorizer implements Authenticator<JwtContext, AuthenticatedUser> {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticatorAuthorizer.class);

  @Override
  public Optional<AuthenticatedUser> authenticate(JwtContext context) {
    try {
      JwtClaims claims = context.getJwtClaims();
      return Optional.of(new AuthenticatedUser(claims.getSubject(), (String) claims.getClaimValue("roles")));
    } catch (Exception e) {
      LOGGER.warn("JWT invalid ({})", e.getMessage());
      return Optional.empty();
    }
  }
}