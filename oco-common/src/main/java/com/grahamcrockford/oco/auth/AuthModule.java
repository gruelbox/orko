package com.grahamcrockford.oco.auth;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.wiring.EnvironmentInitialiser;
import com.grahamcrockford.oco.wiring.WebResource;
import com.okta.jwt.JwtHelper;
import com.okta.jwt.JwtVerifier;
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
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(AuthResource.class);
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class).addBinding().to(AuthEnvironment.class);
  }

  @Provides
  AuthConfiguration authConfiguration(OcoConfiguration ocoConfiguration) {
    return ocoConfiguration.getAuth();
  }

  @Provides
  @Singleton
  AuthenticatorAuthoriser authBase(AuthConfiguration configuration) {
    try {
      JwtHelper helper = new JwtHelper()
        .setIssuerUrl(configuration.okta.issuer)
        .setClientId(configuration.okta.clientId);

      String audience = configuration.okta.audience;
      if (StringUtils.isNotEmpty(audience)) {
        helper.setAudience(audience);
      }
      JwtVerifier jwtVerifier = helper.build();

      return new OktaOAuthAuthenticator(jwtVerifier);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to configure JwtVerifier", e);
    }
  }
}