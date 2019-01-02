package com.gruelbox.orko.auth.okta;

/*-
 * ===============================================================================L
 * Orko Auth
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilderSpec;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.auth.AuthModule;
import com.gruelbox.orko.auth.AuthenticatedUser;
import com.gruelbox.orko.auth.Roles;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import com.nimbusds.oauth2.sdk.ParseException;
import com.okta.jwt.JoseException;
import com.okta.jwt.Jwt;
import com.okta.jwt.JwtHelper;
import com.okta.jwt.JwtVerifier;

import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.setup.Environment;

public class OktaModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(OktaModule.class);

  private final AuthConfiguration auth;

  public OktaModule(AuthConfiguration auth) {
    this.auth = auth;
  }

  @Override
  protected void configure() {
    if (auth.getOkta() != null && auth.getOkta().isEnabled()) {
      Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(OktaConfigurationResource.class);
    }
  }

  @RequestScoped
  @Provides
  Optional<Jwt> jwt(JwtVerifier jwtVerifier, @Named(AuthModule.ACCESS_TOKEN_KEY) Optional<String> accessToken) {
    return accessToken.map(t -> {
      try {
        return jwtVerifier.decodeAccessToken(t);
      } catch (JoseException e) {
        LOGGER.warn("Invalid JWT (" + e.getMessage() + ")");
        return null;
      }
    });
  }

  @Provides
  @Singleton
  JwtVerifier jwtVerifier(AuthConfiguration configuration) throws ParseException, IOException {
    JwtHelper helper = new JwtHelper()
        .setIssuerUrl(configuration.getOkta().getIssuer())
        .setClientId(configuration.getOkta().getClientId());
    String audience = configuration.getOkta().getAudience();
    if (StringUtils.isNotEmpty(audience)) {
      helper.setAudience(audience);
    }
    return helper.build();
  }

  @Provides
  @Singleton
  OktaAuthenticator authenticator(Provider<Optional<Jwt>> jwt, AuthConfiguration configuration, Environment environment) {

    OktaAuthenticator uncached = credentials -> jwt.get().map(j -> new AuthenticatedUser((String) j.getClaims().get("sub"), Roles.TRADER));

    CachingAuthenticator<String, AuthenticatedUser> cached = new CachingAuthenticator<>(
      environment.metrics(),
      uncached,
      CacheBuilderSpec.parse(configuration.getAuthCachePolicy())
    );

    return credentials -> cached.authenticate(credentials);
  }
}
