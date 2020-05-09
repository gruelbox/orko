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

import static com.google.common.net.HttpHeaders.CONTENT_SECURITY_POLICY;
import static com.google.common.net.HttpHeaders.X_CONTENT_TYPE_OPTIONS;
import static com.google.common.net.HttpHeaders.X_FRAME_OPTIONS;
import static com.google.common.net.HttpHeaders.X_XSS_PROTECTION;

import com.google.common.base.Suppliers;
import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.function.Supplier;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.util.URIUtil;

@Singleton
class ClientSecurityHeadersFilter extends AbstractHttpServletFilter {

  private static final String X_CONTENT_SECURITY_POLICY = "X-Content-Security-Policy";
  private static final String IE10 = "MSIE 10";
  private static final String IE11 = "rv:11.0";

  private Supplier<String> contentSecurityPolicy;

  @Inject
  ClientSecurityHeadersFilter(
      Provider<HttpServletRequest> httpServletRequest,
      Provider<AuthConfiguration> authConfiguration) {
    this.contentSecurityPolicy =
        Suppliers.memoize(
            () -> {
              final StringBuilder wssUri = new StringBuilder(128);
              URIUtil.appendSchemeHostPort(
                  wssUri,
                  authConfiguration.get().isHttpsOnly() ? "wss" : "ws",
                  httpServletRequest.get().getServerName(),
                  0); // TODO either use httpServletRequest.get().getServerPort() or proxy header if
              // behind a proxy
              // TODO can bind these as plugins in the future to suit different apps if that's ever
              // an issue
              return "default-src 'none'; "
                  + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                  + "font-src 'self' https://fonts.gstatic.com data:; "
                  + "script-src 'self' https://*.tradingview.com; "
                  + "img-src 'self' data:; "
                  + "frame-src 'self' https://*.tradingview.com; "
                  + "connect-src 'self' https://api.github.com "
                  + wssUri.toString()
                  + "; "
                  + "manifest-src 'self'; "
                  + "frame-ancestors 'self';";
            });
  }

  @Override
  public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    response.setHeader(X_FRAME_OPTIONS, "sameorigin");
    response.setHeader(X_XSS_PROTECTION, "1; mode=block");
    response.setHeader(CONTENT_SECURITY_POLICY, contentSecurityPolicy.get());
    response.setHeader(X_CONTENT_TYPE_OPTIONS, "nosniff");
    String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
    if (userAgent != null && (userAgent.contains(IE10) || userAgent.contains(IE11))) {
      response.setHeader(X_CONTENT_SECURITY_POLICY, contentSecurityPolicy.get());
    }
    chain.doFilter(request, response);
  }
}
