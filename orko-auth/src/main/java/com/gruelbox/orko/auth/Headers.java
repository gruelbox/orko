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
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.iterators.EnumerationIterator;

public final class Headers {

  private Headers() {
    // Not constructable
  }

  public static final String SEC_WEBSOCKET_PROTOCOL = "sec-websocket-protocol";
  public static final String X_FORWARDED_FOR = "X-Forwarded-For";
  public static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
  public static final String X_XSRF_TOKEN = "x-xsrf-token";
  public static final String STRICT_CONTENT_SECURITY = "Strict-Transport-Security";

  public static FluentIterable<String> listForRequest(HttpServletRequest request) {
    return FluentIterable.from(() -> new EnumerationIterator<>(request.getHeaderNames()));
  }
}
