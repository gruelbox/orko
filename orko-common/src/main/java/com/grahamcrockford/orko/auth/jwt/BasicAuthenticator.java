package com.grahamcrockford.orko.auth.jwt;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.grahamcrockford.orko.auth.AuthConfiguration;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.PrincipalImpl;
import io.dropwizard.auth.basic.BasicCredentials;

@Singleton
class BasicAuthenticator implements Authenticator<BasicCredentials, PrincipalImpl> {

  private final AuthConfiguration authConfiguration;

  @Inject
  BasicAuthenticator(AuthConfiguration authConfiguration) {
    this.authConfiguration = authConfiguration;
  }

  @Override
  public Optional<PrincipalImpl> authenticate(BasicCredentials credentials) throws AuthenticationException {
    Preconditions.checkNotNull(authConfiguration.getJwt(), "No JWT auth configuration");
    Preconditions.checkNotNull(authConfiguration.getJwt().getUserName(), "No JWT auth username");
    Preconditions.checkNotNull(authConfiguration.getJwt().getPassword(), "No JWT auth username");
    if (valid(credentials)) {
      return Optional.of(new PrincipalImpl(credentials.getUsername()));
    }
    return Optional.empty();
  }

  private boolean valid(BasicCredentials credentials) {
    return authConfiguration.getJwt().getUserName().equals(credentials.getUsername()) &&
           authConfiguration.getJwt().getPassword().equals(credentials.getPassword());
  }
}