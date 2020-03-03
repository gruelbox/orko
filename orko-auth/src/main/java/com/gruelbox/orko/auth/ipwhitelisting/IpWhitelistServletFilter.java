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
package com.gruelbox.orko.auth.ipwhitelisting;

import static com.gruelbox.orko.db.Transactionally.READ_ONLY_UNIT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.auth.AbstractHttpSecurityServletFilter;
import com.gruelbox.orko.db.Transactionally;
import java.io.IOException;
import javax.annotation.Priority;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

/**
 * Container-level filter which only allows access to the API if the origin IP has been whitelisted.
 * At container level we mop up REST resources, web sockets, static resources etc.
 */
@Singleton
@Priority(100)
class IpWhitelistServletFilter extends AbstractHttpSecurityServletFilter {

  private final IpWhitelistingService ipWhitelisting;
  private final Transactionally transactionally;

  @Inject
  IpWhitelistServletFilter(IpWhitelistingService ipWhitelisting, Transactionally transactionally) {
    this.ipWhitelisting = ipWhitelisting;
    this.transactionally = transactionally;
  }

  @Override
  protected boolean filterHttpRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    if (transactionally.call(READ_ONLY_UNIT, ipWhitelisting::authoriseIp)) {
      return true;
    }
    response.sendError(Response.Status.FORBIDDEN.getStatusCode());
    return false;
  }
}
