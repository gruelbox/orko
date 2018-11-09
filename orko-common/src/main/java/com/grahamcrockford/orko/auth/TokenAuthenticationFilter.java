package com.grahamcrockford.orko.auth;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grahamcrockford.orko.auth.AbstractHttpSecurityServletFilter;
import com.grahamcrockford.orko.auth.CookieHandlers;
import com.grahamcrockford.orko.auth.Roles;

public abstract class TokenAuthenticationFilter<T extends Principal> extends AbstractHttpSecurityServletFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

  protected abstract Optional<T> extractPrincipal(String token) throws TokenValidationException;

  protected abstract boolean authorize(T principal, String role);

  @Override
  protected final boolean filterHttpRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

    String fullPath = request.getContextPath() + request.getServletPath() + request.getPathInfo();

    Optional<String> token = CookieHandlers.ACCESS_TOKEN.read(request);
    if (!token.isPresent()) {
      LOGGER.warn(fullPath + ": no access token");
      response.sendError(401);
      return false;
    }

    try {
      Optional<T> principal = extractPrincipal(token.get());
      if (!principal.isPresent()) {
        LOGGER.warn(fullPath + ": Unauthorised login attempt");
        response.sendError(401);
        return false;
      }
      if (!authorize(principal.get(), Roles.TRADER)) {
        LOGGER.warn(fullPath + ": user [" + principal.get().getName() + "] not authorised");
        response.sendError(401);
        return false;
      }
    } catch (TokenValidationException e) {
      LOGGER.warn(fullPath + ": invalid token");
      response.sendError(401);
      return false;
    }
    return true;

  }


  protected static final class TokenValidationException extends Exception {

    private static final long serialVersionUID = -3006849289786018832L;

    public TokenValidationException(Throwable cause) {
      super(cause);
    }
  }
}