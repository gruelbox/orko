package com.grahamcrockford.oco.auth;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

abstract class AbstractHttpSecurityServletFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Unused
  }

  @Override
  public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (!(request instanceof HttpServletRequest)) {
      chain.doFilter(request, response);
      return;
    }

    HttpServletRequest httpRequest = ((HttpServletRequest)request);
    HttpServletResponse httpResponse = ((HttpServletResponse)response);

    if ("/favicon.ico".equals(httpRequest.getServletPath()) ||
        "/auth".equals(httpRequest.getPathInfo()) ||
        "/auth/config".equals(httpRequest.getPathInfo())) {
      chain.doFilter(request, response);
      return;
    }

    if (filterHttpRequest(httpRequest, httpResponse, chain)) {
      chain.doFilter(request, response);
      return;
    }

    return;
  }

  protected abstract boolean filterHttpRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;

  @Override
  public void destroy() {
    // Unused
  }
}
