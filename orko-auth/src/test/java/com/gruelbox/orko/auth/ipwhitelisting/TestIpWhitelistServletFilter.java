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
package com.gruelbox.orko.auth.ipwhitelisting;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.gruelbox.orko.db.MockTransactionallyFactory;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import org.junit.Test;

public class TestIpWhitelistServletFilter {

  private final IpWhitelistingService ipWhitelisting = mock(IpWhitelistingService.class);
  private final IpWhitelistServletFilter ipWhitelistServletFilter =
      new IpWhitelistServletFilter(
          ipWhitelisting, MockTransactionallyFactory.mockTransactionally());

  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final FilterChain chain = mock(FilterChain.class);

  @Test
  public void testUnfilteredRequestType() throws IOException, ServletException {
    ServletRequest wrongRequest = mock(ServletRequest.class);
    ServletResponse wrongResponse = mock(ServletResponse.class);
    ipWhitelistServletFilter.doFilter(wrongRequest, wrongResponse, chain);
    verifyNoInteractions(chain, ipWhitelisting, wrongRequest, wrongResponse);
  }

  @Test
  public void testFavicon1() throws IOException, ServletException {
    when(request.getServletPath()).thenReturn("/favicon.ico");
    ipWhitelistServletFilter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
    verifyNoInteractions(ipWhitelisting, response);
  }

  @Test
  public void testFavicon2() throws IOException, ServletException {
    when(request.getPathInfo()).thenReturn("/favicon.ico");
    ipWhitelistServletFilter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
    verifyNoInteractions(ipWhitelisting, response);
  }

  @Test
  public void testAuth() throws IOException, ServletException {
    when(request.getPathInfo()).thenReturn("/auth/and/stuff");
    ipWhitelistServletFilter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
    verifyNoInteractions(ipWhitelisting, response);
  }

  @Test
  public void testDoNotPermit() throws IOException, ServletException {
    when(request.getPathInfo()).thenReturn("/anything/else");
    when(ipWhitelisting.authoriseIp()).thenReturn(false);
    ipWhitelistServletFilter.doFilter(request, response, chain);
    verify(response).sendError(Response.Status.FORBIDDEN.getStatusCode());
    verifyNoInteractions(chain);
  }

  @Test
  public void testPermit() throws IOException, ServletException {
    when(request.getPathInfo()).thenReturn("/anything/else");
    when(ipWhitelisting.authoriseIp()).thenReturn(true);
    ipWhitelistServletFilter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
    verifyNoInteractions(response);
  }
}
