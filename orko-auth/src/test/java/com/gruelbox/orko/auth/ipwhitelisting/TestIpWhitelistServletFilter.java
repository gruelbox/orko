package com.gruelbox.orko.auth.ipwhitelisting;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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

  private final IpWhitelisting ipWhitelisting = mock(IpWhitelisting.class);
  private final IpWhitelistServletFilter ipWhitelistServletFilter = new IpWhitelistServletFilter(ipWhitelisting);

  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final FilterChain chain = mock(FilterChain.class);


  @Test
  public void testUnfilteredRequestType() throws IOException, ServletException {
    ServletRequest wrongRequest = mock(ServletRequest.class);
    ServletResponse wrongResponse = mock(ServletResponse.class);
    ipWhitelistServletFilter.doFilter(wrongRequest, wrongResponse, chain);
    verifyZeroInteractions(chain, ipWhitelisting, wrongRequest, wrongResponse);
  }

  @Test
  public void testFavicon1() throws IOException, ServletException {
    when(request.getServletPath()).thenReturn("/favicon.ico");
    ipWhitelistServletFilter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
    verifyZeroInteractions(ipWhitelisting, response);
  }

  @Test
  public void testFavicon2() throws IOException, ServletException {
    when(request.getPathInfo()).thenReturn("/favicon.ico");
    ipWhitelistServletFilter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
    verifyZeroInteractions(ipWhitelisting, response);
  }

  @Test
  public void testAuth() throws IOException, ServletException {
    when(request.getPathInfo()).thenReturn("/auth/and/stuff");
    ipWhitelistServletFilter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
    verifyZeroInteractions(ipWhitelisting, response);
  }

  @Test
  public void testDoNotPermit() throws IOException, ServletException {
    when(request.getPathInfo()).thenReturn("/anything/else");
    when(ipWhitelisting.authoriseIp()).thenReturn(false);
    ipWhitelistServletFilter.doFilter(request, response, chain);
    verify(response).sendError(Response.Status.FORBIDDEN.getStatusCode());
    verifyZeroInteractions(chain);
  }

  @Test
  public void testPermit() throws IOException, ServletException {
    when(request.getPathInfo()).thenReturn("/anything/else");
    when(ipWhitelisting.authoriseIp()).thenReturn(true);
    ipWhitelistServletFilter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
    verifyZeroInteractions(response);
  }
}
