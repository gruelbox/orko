package com.gruelbox.orko.auth.jwt;

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

import java.util.Optional;

import javax.annotation.Priority;

import org.jose4j.jwt.consumer.JwtContext;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.auth.AuthenticatedUser;
import com.gruelbox.orko.auth.TokenAuthenticationFilter;

/**
 * Container-level filter which performs JWT verification. We do it this way so
 * that it'll work for Jersey, Websockets, the admin interface and any static
 * assets. This is the lowest common denominator.
 */
@Singleton
@Priority(102)
class JwtAuthenticationFilter extends TokenAuthenticationFilter {

  private final JwtAuthenticatorAuthorizer authenticator;
  private final Provider<Optional<JwtContext>> jwtContext;

  @Inject
  JwtAuthenticationFilter(JwtAuthenticatorAuthorizer authenticator, Provider<Optional<JwtContext>> jwtContext) {
    this.authenticator = authenticator;
    this.jwtContext = jwtContext;
  }


  @Override
  protected Optional<AuthenticatedUser> extractPrincipal(String token) {
    return jwtContext.get().map(context -> authenticator.authenticate(context).orElse(null));
  }
}
