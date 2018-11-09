package com.grahamcrockford.orko.auth;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.auth.ipwhitelisting.IpWhitelistingModule;
import com.grahamcrockford.orko.auth.jwt.JwtModule;
import com.grahamcrockford.orko.auth.okta.OktaModule;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

public class AuthModule extends AbstractModule {

  private final OrkoConfiguration configuration;

  public AuthModule(OrkoConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    if (configuration.getAuth() != null) {
      install(new GoogleAuthenticatorModule());
      install(new IpWhitelistingModule());
      install(new OktaModule(configuration.getAuth()));
      install(new JwtModule(configuration.getAuth()));
      Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class).addBinding().to(AuthEnvironment.class);
    }
  }

  @Provides
  AuthConfiguration authConfiguration(OrkoConfiguration orkoConfiguration) {
    return orkoConfiguration.getAuth();
  }
}