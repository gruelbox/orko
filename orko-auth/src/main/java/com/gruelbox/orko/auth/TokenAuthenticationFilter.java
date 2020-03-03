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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TokenAuthenticationFilter extends AbstractHttpSecurityServletFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

  protected abstract Optional<AuthenticatedUser> extractPrincipal(String token);

  @Inject
  @Named(AuthModule.BIND_ACCESS_TOKEN_KEY)
  private Provider<Optional<String>> accessToken;

  @Inject private AuthenticatedUserAuthorizer authenticatedUserAuthorizer;

  @Override
  protected final boolean filterHttpRequest(
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    String fullPath = request.getContextPath() + request.getServletPath() + request.getPathInfo();

    Optional<String> token = accessToken.get();
    if (!token.isPresent()) {
      LOGGER.warn("{}: no access token", fullPath);
      response.sendError(401);
      return false;
    }

    Optional<AuthenticatedUser> principal = extractPrincipal(token.get());
    if (!principal.isPresent()) {
      response.sendError(401);
      return false;
    }

    if (!authenticatedUserAuthorizer.authorize(principal.get(), Roles.TRADER)) {
      LOGGER.warn("{}: user [{}] not authorised", fullPath, principal.get().getName());
      response.sendError(401);
      return false;
    }
    return true;
  }
}
