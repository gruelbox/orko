package com.grahamcrockford.oco.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.dropwizard.auth.AuthenticationException;

@Singleton
public class AuthenticatedEndpointConfigurator extends ServerEndpointConfig.Configurator {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatedEndpointConfigurator.class);


  private final AuthenticatorAuthoriser authenticator;

  @Inject
  AuthenticatedEndpointConfigurator(AuthenticatorAuthoriser authenticator) {
    this.authenticator = authenticator;
  }

  @Override
  public void modifyHandshake(final ServerEndpointConfig sec, final HandshakeRequest request, final HandshakeResponse response) {
    try {

      List<String> list = request.getHeaders().get("authorization");
      if (list == null) {
        throw new AuthenticationException("No authorization headers");
      }

      if (list.size() != 1) {
        throw new AuthenticationException("Invalid number of authorization headers (" + list.size() + ")");
      }

      String header = list.get(0);
      if (!header.startsWith("Bearer ")) {
        throw new AuthenticationException("Invalid auth header (" + header + ")");
      }

      String accessToken = header.substring(7);

      Optional<AccessTokenPrincipal> principal = authenticator.authenticate(accessToken);

      if (!principal.isPresent()) {
        throw new AuthenticationException("No matching principal");
      }

      if (!authenticator.authorize(principal.get(), Roles.TRADER)) {
        throw new AuthenticationException("User does not have correct role");
      }

      sec.getUserProperties().put("user", principal);
      super.modifyHandshake(sec, request, response);

    } catch (Exception e) {
      LOGGER.error("Authentication error", e);
      clearAcceptHeader(response);
      super.modifyHandshake(sec, request, response);
    }
  }

  private void clearAcceptHeader(final HandshakeResponse response) {
    response.getHeaders().put(HandshakeResponse.SEC_WEBSOCKET_ACCEPT, new ArrayList<String>());
  }
}