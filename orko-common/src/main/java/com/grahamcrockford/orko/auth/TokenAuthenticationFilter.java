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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.grahamcrockford.orko.auth.AbstractHttpSecurityServletFilter;
import com.grahamcrockford.orko.auth.Roles;

public abstract class TokenAuthenticationFilter<T extends Principal> extends AbstractHttpSecurityServletFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

  protected abstract Optional<T> extractPrincipal(String token);

  protected abstract boolean authorize(T principal, String role);

  @Inject
  @Named(AuthModule.ACCESS_TOKEN_KEY)
  private Provider<Optional<String>> accessToken;


  @Override
  protected final boolean filterHttpRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

    String fullPath = request.getContextPath() + request.getServletPath() + request.getPathInfo();

    Optional<String> token = accessToken.get();
    if (!token.isPresent()) {
      LOGGER.warn(fullPath + ": no access token");
      response.sendError(401);
      return false;
    }

    Optional<T> principal = extractPrincipal(token.get());
    if (!principal.isPresent()) {
      response.sendError(401);
      return false;
    }

    if (!authorize(principal.get(), Roles.TRADER)) {
      LOGGER.warn(fullPath + ": user [" + principal.get().getName() + "] not authorised");
      response.sendError(401);
      return false;
    }
    return true;

  }
}