package com.grahamcrockford.orko.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHttpSecurityServletFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHttpSecurityServletFilter.class);

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

    LOGGER.debug("Request to {} : {} : {}", httpRequest.getContextPath(), httpRequest.getServletPath(), httpRequest.getPathInfo());

    if ("/favicon.ico".equals(httpRequest.getServletPath()) ||
        "/favicon.ico".equals(httpRequest.getPathInfo()) ||
        (httpRequest.getPathInfo() != null && httpRequest.getPathInfo().startsWith("/auth"))) {
      chain.doFilter(request, response);
      return;
    }

    if (filterHttpRequest(httpRequest, httpResponse, chain)) {
      chain.doFilter(request, response);
    }
  }

  protected abstract boolean filterHttpRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;

  @Override
  public void destroy() {
    // Unused
  }
}
