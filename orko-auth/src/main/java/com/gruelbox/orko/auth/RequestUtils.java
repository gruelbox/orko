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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

@RequestScoped
public class RequestUtils {

  private final HttpServletRequest request;
  private final AuthConfiguration authConfiguration;
  private final Supplier<String> sourceIp;

  @Inject
  @VisibleForTesting
  public RequestUtils(HttpServletRequest request, AuthConfiguration authConfiguration) {
    this.request = request;
    this.authConfiguration = authConfiguration;
    this.sourceIp = Suppliers.memoize(this::readSourceIp);
  }

  public String sourceIp() {
    return sourceIp.get();
  }

  private String readSourceIp() {
    if (authConfiguration.isProxied()) {
      String header = request.getHeader(Headers.X_FORWARDED_FOR);
      if (StringUtils.isEmpty(header)) {
        throw new IllegalStateException(
            "Configured to assume application is behind a proxy but the forward header has not been provided. "
                + "Headers available: "
                + Headers.listForRequest(request).toList());
      }
      return header.split(",")[0];
    } else {
      return request.getRemoteAddr();
    }
  }
}
