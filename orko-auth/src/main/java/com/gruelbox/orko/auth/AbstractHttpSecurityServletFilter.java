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

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHttpSecurityServletFilter extends AbstractHttpServletFilter {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractHttpSecurityServletFilter.class);

  @Override
  public final void doFilter(
      HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain)
      throws IOException, ServletException {
    LOGGER.debug(
        "Request to {} : {} : {}",
        httpRequest.getContextPath(),
        httpRequest.getServletPath(),
        httpRequest.getPathInfo());

    // TODO security-bypassed URI patterns should be provided on a modular
    // plugin basis
    if ("/favicon.ico".equals(httpRequest.getServletPath())
        || "/favicon.ico".equals(httpRequest.getPathInfo())
        || (httpRequest.getPathInfo() != null && httpRequest.getPathInfo().startsWith("/auth"))) {
      chain.doFilter(httpRequest, httpResponse);
      return;
    }

    if (filterHttpRequest(httpRequest, httpResponse)) {
      chain.doFilter(httpRequest, httpResponse);
    }
  }

  protected abstract boolean filterHttpRequest(
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException;
}
