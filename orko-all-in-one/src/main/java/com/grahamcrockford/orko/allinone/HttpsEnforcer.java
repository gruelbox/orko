package com.grahamcrockford.orko.allinone;

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

public class HttpsEnforcer implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpsEnforcer.class);

  public static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    if (request.getHeader(X_FORWARDED_PROTO) != null) {
      if (request.getHeader(X_FORWARDED_PROTO).indexOf("https") != 0) {
        String pathInfo = (request.getPathInfo() != null) ? request.getPathInfo() : "";
        String redirect = "https://" + request.getServerName() + pathInfo;
        LOGGER.error("Unsecured access redirected to [{}]", redirect);
        response.sendRedirect(redirect);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  @Override
  public void destroy() { }
}