package com.grahamcrockford.oco.web.auth;

import java.util.Optional;

import com.okta.jwt.JoseException;
import com.okta.jwt.Jwt;
import com.okta.jwt.JwtVerifier;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;

public class OktaOAuthAuthenticator implements Authenticator<String, AccessTokenPrincipal>, Authorizer<AccessTokenPrincipal> {

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
      throw new AuthenticationException(e);
    }
  }

  @Override
  public boolean authorize(AccessTokenPrincipal principal, String role) {
    // TODO consider whether to use roles later
    return true;
  }
}