package com.grahamcrockford.orko.auth.jwt;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.warrenstrange.googleauth.GoogleAuthenticator;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.PrincipalImpl;

@Singleton
class LoginAuthenticator implements Authenticator<LoginRequest, PrincipalImpl> {

  private final AuthConfiguration authConfiguration;
  private final GoogleAuthenticator googleAuthenticator;

  @Inject
  LoginAuthenticator(AuthConfiguration authConfiguration, GoogleAuthenticator googleAuthenticator) {
    this.authConfiguration = authConfiguration;
    this.googleAuthenticator = googleAuthenticator;
  }

  @Override
  public Optional<PrincipalImpl> authenticate(LoginRequest credentials) throws AuthenticationException {
    Preconditions.checkNotNull(authConfiguration.getJwt(), "No JWT auth configuration");
    Preconditions.checkNotNull(authConfiguration.getJwt().getUserName(), "No JWT auth username");
    Preconditions.checkNotNull(authConfiguration.getJwt().getPassword(), "No JWT auth username");
    if (valid(credentials)) {
      return Optional.of(new PrincipalImpl(credentials.getUsername()));
    }
    return Optional.empty();
  }

  private boolean valid(LoginRequest credentials) {
    return authConfiguration.getJwt().getUserName().equals(credentials.getUsername()) &&
           authConfiguration.getJwt().getPassword().equals(credentials.getPassword()) &&
           passesSecondFactor(credentials);
  }

  private boolean passesSecondFactor(LoginRequest credentials) {
    if (StringUtils.isEmpty(authConfiguration.getSecretKey()))
      return true;
    if (credentials.getSecondFactor() == null) {
      return false;
    }
    return googleAuthenticator.authorize(authConfiguration.getSecretKey(), credentials.getSecondFactor());
  }
}