package com.grahamcrockford.orko.auth;

import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.annotation.Priority;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

/**
 * If we encounter Sec-WebSocket-Protocol header, translate the request
 * into token authentication (since this is the only way we can get authentication headers
 * out in Websocket handshake requests).
 */
@Singleton
@Priority(101)
class ProtocolToBearerTranslationFilter extends AbstractHttpSecurityServletFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolToBearerTranslationFilter.class);

  @Override
  protected boolean filterHttpRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

    String protocol = request.getHeader(Headers.SEC_WEBSOCKET_PROTOCOL);
    if (protocol == null || !protocol.startsWith("auth, ") || protocol.length() < 7) {
      return true;
    }
    String accessToken = protocol.substring(6);

    if (accessToken != null) {

      LOGGER.debug("{}: rewriting Sec-WebSocket-Protocol header as Bearer", request.getPathInfo());
      response.addHeader("Sec-WebSocket-Protocol", "auth");
      chain.doFilter(
        new HttpServletRequestWrapper(request) {

          @Override
          public String getHeader(String name) {
            String lowerCase = name.toLowerCase();
            if (Headers.AUTHORIZATION.equals(lowerCase)) {
              return "Bearer " + accessToken;
            } else if (Headers.SEC_WEBSOCKET_PROTOCOL.equals(lowerCase)) {
              return null;
            } else {
              return super.getHeader(name);
            }
          }

          @Override
          public Enumeration<String> getHeaders(String name) {
            String lowerCase = name.toLowerCase();
            if (Headers.SEC_WEBSOCKET_PROTOCOL.equals(lowerCase)) {
              return new EmptyEnumeration();
            } else {
              return super.getHeaders(name);
            }
          }

        },
        response
      );
    }

    return false;
  }

  private final class EmptyEnumeration implements Enumeration<String> {
    @Override
    public boolean hasMoreElements() {
      return false;
    }

    @Override
    public String nextElement() {
      throw new NoSuchElementException();
    }
  }
}