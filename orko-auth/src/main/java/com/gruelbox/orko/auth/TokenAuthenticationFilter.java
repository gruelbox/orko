package com.gruelbox.orko.auth;

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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public abstract class TokenAuthenticationFilter extends AbstractHttpSecurityServletFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

  protected abstract Optional<AuthenticatedUser> extractPrincipal(String token);

  @Inject
  @Named(AuthModule.ACCESS_TOKEN_KEY)
  private Provider<Optional<String>> accessToken;

  @Inject
  private AuthenticatedUserAuthorizer authenticatedUserAuthorizer;


  @Override
  protected final boolean filterHttpRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

    String fullPath = request.getContextPath() + request.getServletPath() + request.getPathInfo();

    Optional<String> token = accessToken.get();
    if (!token.isPresent()) {
      LOGGER.warn(fullPath + ": no access token");
      response.sendError(401);
      return false;
    }

    Optional<AuthenticatedUser> principal = extractPrincipal(token.get());
    if (!principal.isPresent()) {
      response.sendError(401);
      return false;
    }

    if (!authenticatedUserAuthorizer.authorize(principal.get(), Roles.TRADER)) {
      LOGGER.warn(fullPath + ": user [" + principal.get().getName() + "] not authorised");
      response.sendError(401);
      return false;
    }
    return true;

  }
}
