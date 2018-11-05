package com.grahamcrockford.orko.auth.okta;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.okta.jwt.JoseException;
import com.okta.jwt.Jwt;
import com.okta.jwt.JwtVerifier;

import io.dropwizard.auth.AuthenticationException;

class OktaOAuthAuthenticator implements OrkoAuthenticator {

  private static final Logger LOGGER = LoggerFactory.getLogger(OktaOAuthAuthenticator.class);

  private final JwtVerifier jwtVerifier;

  public OktaOAuthAuthenticator(JwtVerifier jwtVerifier) {
    this.jwtVerifier = jwtVerifier;
  }

  @Override
  public Optional<AccessTokenPrincipal> authenticate(String accessToken) throws AuthenticationException {
    try {
      Jwt jwt = jwtVerifier.decodeAccessToken(accessToken);
      // if we made it this far we have a valid jwt
      return Optional.of(new AccessTokenPrincipal(jwt));
    } catch (JoseException e) {
      LOGGER.warn("JWT invalid (" + e.getMessage() + ")");
      return Optional.empty();
    } catch (Exception e) {
      throw new AuthenticationException(e);
    }
  }
}