package com.grahamcrockford.orko.auth;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.Priority;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.dropwizard.auth.AuthenticationException;

/**
 * Container-level filter which performs JWT verification.  We do it this way so that it'll
 * work for Jersey, Websockets, the admin interface and any static assets. This is the lowest
 * common denominator.
 */
@Singleton
@Priority(102)
class BearerAuthenticationFilter extends AbstractHttpSecurityServletFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthenticationFilter.class);

  private final OrkoAuthenticator authenticator;
  private final OrkoAuthorizer authorizer;


  @Inject
  BearerAuthenticationFilter(OrkoAuthenticator authenticator, OrkoAuthorizer authorizer) {
    this.authenticator = authenticator;
    this.authorizer = authorizer;
  }

  @Override
  protected boolean filterHttpRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

    String fullPath = request.getContextPath() + request.getServletPath() + request.getPathInfo();

    String authorization = request.getHeader(Headers.AUTHORIZATION);
    if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() <= 7) {
      LOGGER.warn(fullPath + ": invalid auth header: " + authorization);
      response.sendError(401);
      return false;
    }

    String accessToken = authorization.substring(7);

    try {
      Optional<AccessTokenPrincipal> principal = authenticator.authenticate(accessToken);
      if (!principal.isPresent()) {
        LOGGER.warn(fullPath + ": Unauthorised login attempt");
        response.sendError(401);
        return false;
      }
      if (!authorizer.authorize(principal.get(), Roles.TRADER)) {
        LOGGER.warn(fullPath + ": user [" + principal.get().getName() + "] not authorised");
        response.sendError(401);
        return false;
      }
    } catch (AuthenticationException e) {
      LOGGER.warn(fullPath + ": invalid token", e);
      response.sendError(401);
      return false;
    }

    return true;
  }

}
