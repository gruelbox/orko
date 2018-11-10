package com.grahamcrockford.orko.auth;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.grahamcrockford.orko.auth.ipwhitelisting.IpWhitelistingEnvironment;
import com.grahamcrockford.orko.auth.jwt.JwtEnvironment;
import com.grahamcrockford.orko.auth.okta.OktaEnvironment;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
class AuthEnvironment implements EnvironmentInitialiser {

  private final IpWhitelistingEnvironment ipWhitelistingEnvironment;
  private final OktaEnvironment oktaEnvironment;
  private final JwtEnvironment jwtEnvironment;


  @Inject
  AuthEnvironment(IpWhitelistingEnvironment ipWhitelistingEnvironment,
                  OktaEnvironment oktaEnvironment,
                  JwtEnvironment jwtEnvironment) {
    this.ipWhitelistingEnvironment = ipWhitelistingEnvironment;
    this.oktaEnvironment = oktaEnvironment;
    this.jwtEnvironment = jwtEnvironment;
  }

  @Override
  public void init(Environment environment) {
    ipWhitelistingEnvironment.init(environment);
    oktaEnvironment.init(environment);
    jwtEnvironment.init(environment);
  }
}