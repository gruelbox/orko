package com.grahamcrockford.oco.auth;

import java.io.IOException;

import javax.annotation.Priority;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Container-level filter which only allows access to {@link AuthResource} if
 * the origin IP has been whitelisted.  At container level we mop up REST resources, web
 * sockets, static resources etc.
 */
@Singleton
@Priority(100)
class IpWhitelistServletFilter implements Filter {

  private final IpWhitelisting ipWhitelisting;

  @Inject
  IpWhitelistServletFilter(IpWhitelisting ipWhitelisting) {
    this.ipWhitelisting = ipWhitelisting;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Unused
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    if (!(request instanceof HttpServletRequest)) {
      throw new ServletException("Bad request");
    }

    HttpServletRequest httpRequest = ((HttpServletRequest)request);
    HttpServletResponse httpResponse = ((HttpServletResponse)response);

    if (!"/auth".equals(httpRequest.getPathInfo()) && !ipWhitelisting.authoriseIp()) {
      httpResponse.sendError(Response.Status.FORBIDDEN.getStatusCode());
      return;
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    // Unused
  }
}
