package com.grahamcrockford.oco.auth;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.grahamcrockford.oco.OcoConfiguration;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;

public class AuthModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(GoogleAuthenticator.class).toInstance(
      new GoogleAuthenticator(
        new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder().build()
      )
    );
  }

  @Provides
  AuthConfiguration authConfiguration(OcoConfiguration ocoConfiguration) {
    return ocoConfiguration.getAuth();
  }
}