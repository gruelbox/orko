package com.grahamcrockford.orko.auth.okta;

import org.apache.commons.lang3.StringUtils;

import com.google.common.cache.CacheBuilderSpec;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.wiring.WebResource;
import com.okta.jwt.JwtHelper;
import com.okta.jwt.JwtVerifier;

import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.setup.Environment;

public class OktaModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(OktaResource.class);
  }

  @Provides
  @Singleton
  OrkoAuthenticator authenticator(AuthConfiguration configuration, Environment environment) {

    OrkoAuthenticator uncached;
    try {
      JwtHelper helper = new JwtHelper()
        .setIssuerUrl(configuration.getOkta().getIssuer())
        .setClientId(configuration.getOkta().getClientId());

      String audience = configuration.getOkta().getAudience();
      if (StringUtils.isNotEmpty(audience)) {
        helper.setAudience(audience);
      }
      JwtVerifier jwtVerifier = helper.build();

      uncached = new OktaOAuthAuthenticator(jwtVerifier);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to configure JwtVerifier", e);
    }

    CachingAuthenticator<String, AccessTokenPrincipal> cached = new CachingAuthenticator<String, AccessTokenPrincipal>(
      environment.metrics(),
      uncached,
      CacheBuilderSpec.parse(configuration.getAuthCachePolicy())
    );

    return credentials -> cached.authenticate(credentials);
  }


  @Provides
  @Singleton
  OrkoAuthorizer authorizer() {
    return (principal, role) -> true;
  }

}