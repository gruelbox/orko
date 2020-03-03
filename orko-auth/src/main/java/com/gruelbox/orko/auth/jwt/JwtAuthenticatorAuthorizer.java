/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.auth.jwt;

import com.google.inject.Singleton;
import com.gruelbox.orko.auth.AuthenticatedUser;
import io.dropwizard.auth.Authenticator;
import java.util.Optional;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class JwtAuthenticatorAuthorizer implements Authenticator<JwtContext, AuthenticatedUser> {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticatorAuthorizer.class);

  @Override
  public Optional<AuthenticatedUser> authenticate(JwtContext context) {
    try {
      JwtClaims claims = context.getJwtClaims();
      return Optional.of(
          new AuthenticatedUser(claims.getSubject(), (String) claims.getClaimValue("roles")));
    } catch (Exception e) {
      LOGGER.warn("JWT invalid ({})", e.getMessage());
      return Optional.empty();
    }
  }
}
