package com.grahamcrockford.oco.auth;

import javax.inject.Inject;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import com.google.inject.Singleton;
import com.grahamcrockford.oco.EnvironmentInitialiser;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Environment;

@Singleton
class AuthEnvironment implements EnvironmentInitialiser {

  private final SimpleAuthenticator authenticator;

  @Inject
  AuthEnvironment(SimpleAuthenticator authenticator) {
    this.authenticator = authenticator;
  }

  @Override
  public void init(Environment environment) {
    // Auth
    environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
      .setAuthenticator(authenticator)
      .setAuthorizer(authenticator)
      .setRealm("SUPER SECRET STUFF")
      .buildAuthFilter()
    ));
    environment.jersey().register(RolesAllowedDynamicFeature.class);
  }
}