package com.grahamcrockford.orko.auth.jwt;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.Priority;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.auth.AbstractHttpSecurityServletFilter;
import com.grahamcrockford.orko.auth.Roles;

/**
 * Container-level filter which performs JWT verification. We do it this way so
 * that it'll work for Jersey, Websockets, the admin interface and any static
 * assets. This is the lowest common denominator.
 */
@Singleton
@Priority(102)
class JwtAuthenticationFilter extends AbstractHttpSecurityServletFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private static final String COOKIE = "authtoken";
  private static final String PREFIX = "Bearer";

  private final JwtAuthenticatorAuthorizer authenticator;
  private final JwtConsumer jwtConsumer;

  @Inject
  JwtAuthenticationFilter(JwtAuthenticatorAuthorizer authenticator, JwtConsumer jwtConsumer) {
    this.authenticator = authenticator;
    this.jwtConsumer = jwtConsumer;
  }

  @Override
  protected boolean filterHttpRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

    String fullPath = request.getContextPath() + request.getServletPath() + request.getPathInfo();

    Optional<String> token = token(request);
    if (!token.isPresent()) {
      LOGGER.warn(fullPath + ": no auth header");
      response.sendError(401);
      return false;
    }

    try {
      JwtContext jwtContext = jwtConsumer.process(token.get());
      Optional<AuthenticatedUser> principal = authenticator.authenticate(jwtContext);
      if (!principal.isPresent()) {
        LOGGER.warn(fullPath + ": Unauthorised login attempt");
        response.sendError(401);
        return false;
      }
      if (!authenticator.authorize(principal.get(), Roles.TRADER)) {
        LOGGER.warn(fullPath + ": user [" + principal.get().getName() + "] not authorised");
        response.sendError(401);
        return false;
      }
    } catch (InvalidJwtException e) {
      LOGGER.warn(fullPath + ": invalid token", e);
      response.sendError(401);
      return false;
    }
    return true;

  }

  private Optional<String> token(HttpServletRequest request) {
    final Optional<String> headerToken = headerToken(request);
    return headerToken.isPresent() ? headerToken : cookieToken(request);
  }

  private Optional<String> headerToken(HttpServletRequest request) {
    final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null) {
      int space = header.indexOf(' ');
      if (space > 0) {
        final String method = header.substring(0, space);
        if (PREFIX.equalsIgnoreCase(method)) {
          final String rawToken = header.substring(space + 1);
          return Optional.of(rawToken);
        }
      }
    }
    return Optional.empty();
  }

  private Optional<String> cookieToken(HttpServletRequest request) {
    if (request.getCookies() == null)
      return Optional.empty();
    return FluentIterable.from(request.getCookies())
        .firstMatch(cookie -> COOKIE.equals(cookie.getName()))
        .transform(Cookie::getValue)
        .toJavaUtil();
  }
}
