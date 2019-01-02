package com.gruelbox.orko.auth;

/*-
 * ===============================================================================L
 * Orko Auth
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

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
