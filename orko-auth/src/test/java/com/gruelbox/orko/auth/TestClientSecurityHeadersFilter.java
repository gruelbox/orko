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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.net.HttpHeaders;
import com.google.inject.util.Providers;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TestClientSecurityHeadersFilter {

  private static final String IE10_HEADER = "rhubarb rhubarb MSIE 10 rhubarb";
  private static final String IE_11_HEADER = "rhubarb rhubarb rv:11.0 rhubarb";
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain chain;
  @Mock private AuthConfiguration authConfiguration;
  private ClientSecurityHeadersFilter filter;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    filter =
        new ClientSecurityHeadersFilter(Providers.of(request), Providers.of(authConfiguration));
  }

  @Test
  public void testClientSecurityHeadersFilter() throws IOException, ServletException {

    when(request.getServerName()).thenReturn("gruelbox.com");

    filter.doFilter(request, response, chain);

    verify(response).setHeader(X_FRAME_OPTIONS, "sameorigin");
    verify(response).setHeader(X_XSS_PROTECTION, "1; mode=block");
    verify(response).setHeader(X_CONTENT_TYPE_OPTIONS, "nosniff");
    verify(response)
        .setHeader(
            CONTENT_SECURITY_POLICY,
            "default-src 'none'; "
                + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                + "font-src 'self' https://fonts.gstatic.com data:; "
                + "script-src 'self' https://*.tradingview.com; "
                + "img-src 'self' data:; "
                + "frame-src 'self' https://*.tradingview.com; "
                + "connect-src 'self' https://api.github.com ws://gruelbox.com; "
                + "manifest-src 'self'; "
                + "frame-ancestors 'self';");
  }

  @Test
  public void testWss() throws IOException, ServletException {

    when(request.getServerName()).thenReturn("gruelbox.com");
    when(authConfiguration.isHttpsOnly()).thenReturn(true);

    filter.doFilter(request, response, chain);

    verify(response)
        .setHeader(
            CONTENT_SECURITY_POLICY,
            "default-src 'none'; "
                + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                + "font-src 'self' https://fonts.gstatic.com data:; "
                + "script-src 'self' https://*.tradingview.com; "
                + "img-src 'self' data:; "
                + "frame-src 'self' https://*.tradingview.com; "
                + "connect-src 'self' https://api.github.com wss://gruelbox.com; "
                + "manifest-src 'self'; "
                + "frame-ancestors 'self';");
  }

  @Test
  public void testIe10Ws() throws IOException, ServletException {

    when(request.getServerName()).thenReturn("github.com");
    when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(IE10_HEADER);

    filter.doFilter(request, response, chain);

    verify(response)
        .setHeader(
            "X-Content-Security-Policy",
            "default-src 'none'; "
                + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                + "font-src 'self' https://fonts.gstatic.com data:; "
                + "script-src 'self' https://*.tradingview.com; "
                + "img-src 'self' data:; "
                + "frame-src 'self' https://*.tradingview.com; "
                + "connect-src 'self' https://api.github.com ws://github.com; "
                + "manifest-src 'self'; "
                + "frame-ancestors 'self';");
  }

  @Test
  public void testIe10Wss() throws IOException, ServletException {

    when(request.getServerName()).thenReturn("github.com");
    when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(IE10_HEADER);
    when(authConfiguration.isHttpsOnly()).thenReturn(true);

    filter.doFilter(request, response, chain);

    verify(response)
        .setHeader(
            "X-Content-Security-Policy",
            "default-src 'none'; "
                + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                + "font-src 'self' https://fonts.gstatic.com data:; "
                + "script-src 'self' https://*.tradingview.com; "
                + "img-src 'self' data:; "
                + "frame-src 'self' https://*.tradingview.com; "
                + "connect-src 'self' https://api.github.com wss://github.com; "
                + "manifest-src 'self'; "
                + "frame-ancestors 'self';");
  }

  @Test
  public void testIe11Ws() throws IOException, ServletException {

    when(request.getServerName()).thenReturn("github.com");
    when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(IE_11_HEADER);

    filter.doFilter(request, response, chain);

    verify(response)
        .setHeader(
            "X-Content-Security-Policy",
            "default-src 'none'; "
                + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                + "font-src 'self' https://fonts.gstatic.com data:; "
                + "script-src 'self' https://*.tradingview.com; "
                + "img-src 'self' data:; "
                + "frame-src 'self' https://*.tradingview.com; "
                + "connect-src 'self' https://api.github.com ws://github.com; "
                + "manifest-src 'self'; "
                + "frame-ancestors 'self';");
  }

  @Test
  public void testIe11Wss() throws IOException, ServletException {

    when(request.getServerName()).thenReturn("github.com");
    when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(IE_11_HEADER);
    when(authConfiguration.isHttpsOnly()).thenReturn(true);

    filter.doFilter(request, response, chain);

    verify(response)
        .setHeader(
            "X-Content-Security-Policy",
            "default-src 'none'; "
                + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                + "font-src 'self' https://fonts.gstatic.com data:; "
                + "script-src 'self' https://*.tradingview.com; "
                + "img-src 'self' data:; "
                + "frame-src 'self' https://*.tradingview.com; "
                + "connect-src 'self' https://api.github.com wss://github.com; "
                + "manifest-src 'self'; "
                + "frame-ancestors 'self';");
  }
}
