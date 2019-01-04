/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.auth.AbstractHttpSecurityServletFilter;
import com.gruelbox.orko.auth.Headers;

@Singleton
class JwtXsrfProtectionFilter extends AbstractHttpSecurityServletFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtXsrfProtectionFilter.class);

  private final Provider<Optional<JwtContext>> jwtContext;

  @Inject
  JwtXsrfProtectionFilter(Provider<Optional<JwtContext>> jwtContext) {
    this.jwtContext = jwtContext;
  }

  @Override
  protected final boolean filterHttpRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

    String fullPath = request.getContextPath() + request.getServletPath() + request.getPathInfo();

    // Slightly ugly. We want to let the DB dump API through our XSRF controls
    // since this is normally user-initiated.
    if (fullPath.equals("/api/db.zip") || fullPath.equals("/api/db/debug") ) {
      return true;
    }

    Optional<String> claim = jwtContext.get()
        .map(JwtContext::getJwtClaims)
        .map(claims -> {
          try {
            return claims.getClaimValue("xsrf", String.class);
          } catch (MalformedClaimException e) {
            LOGGER.warn(fullPath + ": malformed XSRF claim");
            return null;
          }
        });

    if (!claim.isPresent()) {
      LOGGER.warn(fullPath + ": failed cross-site scripting check (no claim)");
      response.sendError(401);
      return false;
    }

    String xsrf = request.getHeader(Headers.X_XSRF_TOKEN);
    if (!claim.get().equals(xsrf)) {
      LOGGER.warn(fullPath + ": failed cross-site scripting check");
      response.sendError(401);
      return false;
    }

    return true;
  }
}
