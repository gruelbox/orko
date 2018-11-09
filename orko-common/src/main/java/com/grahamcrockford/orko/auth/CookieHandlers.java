package com.grahamcrockford.orko.auth;

import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;

import com.google.common.collect.FluentIterable;

public enum CookieHandlers {

  ACCESS_TOKEN("accessToken");

  private final String name;

  private CookieHandlers(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Optional<String> read(HttpServletRequest request) {
    if (request.getCookies() == null)
      return Optional.empty();
    return FluentIterable.from(request.getCookies())
        .firstMatch(cookie -> getName().equals(cookie.getName()))
        .transform(Cookie::getValue)
        .toJavaUtil();
  }

  public NewCookie create(String token, AuthConfiguration authConfiguration) {
    return new NewCookie(getName(), token, "/", null, 1, null,
        authConfiguration.getJwt().getExpirationMinutes() * 60, null,
        authConfiguration.isHttpsOnly(), false);
  }
}