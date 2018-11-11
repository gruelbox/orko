package com.grahamcrockford.orko.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.iterators.EnumerationIterator;

import com.google.common.collect.FluentIterable;

public class Headers {

  public static final String SEC_WEBSOCKET_PROTOCOL = "sec-websocket-protocol";
  public static final String X_FORWARDED_FOR = "X-Forwarded-For";
  public static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
  public static final String X_XSRF_TOKEN = "x-xsrf-token";
  public static final String STRICT_CONTENT_SECURITY = "Strict-Transport-Security";

  public static FluentIterable<String> listForRequest(HttpServletRequest request) {
    return FluentIterable.from(() -> new EnumerationIterator<String>(request.getHeaderNames()));
  }
}
