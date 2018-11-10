package com.grahamcrockford.orko.auth;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grahamcrockford.orko.auth.AbstractHttpSecurityServletFilter;
import com.grahamcrockford.orko.auth.CookieHandlers;

public class XsrfProtectionFilter extends AbstractHttpSecurityServletFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(XsrfProtectionFilter.class);

  @Override
  protected final boolean filterHttpRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

    String fullPath = request.getContextPath() + request.getServletPath() + request.getPathInfo();

    Optional<String> token = CookieHandlers.ACCESS_TOKEN.read(request);
    if (!token.isPresent()) {
      LOGGER.warn(fullPath + ": no access token");
      response.sendError(401);
      return false;
    }

    String xsrf = request.getHeader(Headers.X_XSRF_TOKEN);
    if (!token.get().equals(xsrf)) {
      LOGGER.warn(fullPath + ": failed cross-site scripting check");
      response.sendError(401);
      return false;
    }

    return true;
  }
}