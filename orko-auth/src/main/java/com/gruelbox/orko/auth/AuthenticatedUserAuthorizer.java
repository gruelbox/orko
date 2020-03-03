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
package com.gruelbox.orko.auth;

import io.dropwizard.auth.Authorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticatedUserAuthorizer implements Authorizer<AuthenticatedUser> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatedUserAuthorizer.class);

  @Override
  public boolean authorize(AuthenticatedUser authenticatedUser, String role) {
    if (authenticatedUser == null) {
      LOGGER.warn("No user provided for authorization");
      return false;
    }
    return authenticatedUser.getRoles().contains(role);
  }
}
