/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.auth;

import com.google.common.collect.FluentIterable;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;

public enum CookieHandlers {
  ACCESS_TOKEN(AuthModule.BIND_ACCESS_TOKEN_KEY);

  private final String name;

  private CookieHandlers(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Optional<String> read(HttpServletRequest request) {
    if (request.getCookies() == null) return Optional.empty();
    return FluentIterable.from(request.getCookies())
        .firstMatch(cookie -> getName().equals(cookie.getName()))
        .transform(Cookie::getValue)
        .toJavaUtil();
  }

  public NewCookie create(String token, AuthConfiguration authConfiguration) {
    return new NewCookie(
        getName(),
        token,
        "/",
        null,
        1,
        null,
        authConfiguration.getJwt().getExpirationMinutes() * 60,
        null,
        authConfiguration.isHttpsOnly(),
        true);
  }
}
