package com.gruelbox.orko.auth;

import com.google.inject.AbstractModule;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;

public class GoogleAuthenticatorModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(IGoogleAuthenticator.class).toInstance(
        new GoogleAuthenticator(
          new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder().build()
        )
      );
  }
}