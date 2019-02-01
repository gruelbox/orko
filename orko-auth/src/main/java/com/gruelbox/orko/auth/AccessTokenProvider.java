/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
