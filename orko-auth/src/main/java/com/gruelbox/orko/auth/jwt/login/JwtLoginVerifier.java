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
package com.gruelbox.orko.auth.jwt.login;

import com.google.common.base.Preconditions;
import com.gruelbox.orko.auth.Hasher;
import com.gruelbox.orko.auth.jwt.JwtConfiguration;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.PrincipalImpl;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class JwtLoginVerifier implements Authenticator<LoginRequest, PrincipalImpl> {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtLoginVerifier.class);

  private final JwtConfiguration config;
  private final IGoogleAuthenticator googleAuthenticator;
  private final Hasher hasher;

  @Inject
  JwtLoginVerifier(
      JwtConfiguration config, IGoogleAuthenticator googleAuthenticator, Hasher hasher) {
    this.config = config;
    this.googleAuthenticator = googleAuthenticator;
    this.hasher = hasher;
  }

  @Override
  public Optional<PrincipalImpl> authenticate(LoginRequest credentials)
      throws AuthenticationException {
    Preconditions.checkNotNull(config, "No JWT auth configuration");
    Preconditions.checkNotNull(config, "No JWT auth username");
    Preconditions.checkNotNull(config, "No JWT auth username");
    if (valid(credentials)) {
      return Optional.of(new PrincipalImpl(credentials.getUsername()));
    }
    if (credentials == null) {
      LOGGER.warn("Invalid login attempt (no credentials)");
    } else {
      LOGGER.warn("Invalid login attempt by [{}]", credentials.getUsername());
    }
    return Optional.empty();
  }

  private boolean valid(LoginRequest credentials) {
    return credentials != null
        && userMatches(credentials)
        && passwordMatches(credentials)
        && passesSecondFactor(credentials);
  }

  private boolean userMatches(LoginRequest credentials) {
    return config.getUserName().equals(credentials.getUsername());
  }

  private boolean passwordMatches(LoginRequest credentials) {
    if (hasher.isHash(config.getPassword())) {
      return config
          .getPassword()
          .equals(hasher.hash(credentials.getPassword(), config.getPasswordSalt()));
    } else {
      return config.getPassword().equals(credentials.getPassword());
    }
  }

  private boolean passesSecondFactor(LoginRequest credentials) {
    if (StringUtils.isEmpty(config.getSecondFactorSecret())) return true;
    if (credentials.getSecondFactor() == null) {
      return false;
    }
    return googleAuthenticator.authorize(
        config.getSecondFactorSecret(), credentials.getSecondFactor());
  }
}
