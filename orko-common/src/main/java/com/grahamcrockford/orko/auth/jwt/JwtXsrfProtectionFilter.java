package com.grahamcrockford.orko.auth.jwt;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.auth.AbstractHttpSecurityServletFilter;
import com.grahamcrockford.orko.auth.Headers;

@Singleton
class JwtXsrfProtectionFilter extends AbstractHttpSecurityServletFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtXsrfProtectionFilter.class);

  private final Provider<Optional<JwtContext>> jwtContext;

  @Inject
  JwtXsrfProtectionFilter(Provider<Optional<JwtContext>> jwtContext) {
    this.jwtContext = jwtContext;
  }

  @Override
  protected final boolean filterHttpRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

    String fullPath = request.getContextPath() + request.getServletPath() + request.getPathInfo();

    Optional<String> claim = jwtContext.get()
        .map(JwtContext::getJwtClaims)
        .map(claims -> {
          try {
            return claims.getClaimValue("xsrf", String.class);
          } catch (MalformedClaimException e) {
            LOGGER.warn(fullPath + ": malformed XSRF claim");
            return null;
          }
        });

    if (!claim.isPresent()) {
      LOGGER.warn(fullPath + ": failed cross-site scripting check (no claim)");
      response.sendError(401);
      return false;
    }

    String xsrf = request.getHeader(Headers.X_XSRF_TOKEN);
    if (!claim.get().equals(xsrf)) {
      LOGGER.warn(fullPath + ": failed cross-site scripting check");
      response.sendError(401);
      return false;
    }

    return true;
  }
}