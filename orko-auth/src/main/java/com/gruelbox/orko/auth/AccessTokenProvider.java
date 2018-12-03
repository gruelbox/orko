package com.gruelbox.orko.auth;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

class AccessTokenProvider implements Provider<Optional<String>> {

  private final Provider<HttpServletRequest> request;

  @Inject
  AccessTokenProvider(Provider<HttpServletRequest> request) {
    this.request = request;
  }

  @Override
  public Optional<String> get() {
    return CookieHandlers.ACCESS_TOKEN.read(request.get());
  }

}
