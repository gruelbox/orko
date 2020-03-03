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

import com.codahale.metrics.annotation.Timed;
import com.gruelbox.orko.auth.blacklist.Blacklisting;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import io.dropwizard.hibernate.UnitOfWork;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/auth")
@Singleton
public class IpWhitelistingResource implements WebResource {

  private final IpWhitelistingService ipWhitelisting;
  private final Blacklisting blacklisting;

  @Inject
  IpWhitelistingResource(IpWhitelistingService ipWhitelisting, Blacklisting blacklisting) {
    this.ipWhitelisting = ipWhitelisting;
    this.blacklisting = blacklisting;
  }

  @DELETE
  @Timed
  @Produces(MediaType.TEXT_PLAIN)
  @UnitOfWork
  public Response delete() {
    if (blacklisting.isBlacklisted())
      return Response.status(Status.TOO_MANY_REQUESTS)
          .entity("Too many requests")
          .type(MediaType.TEXT_PLAIN)
          .build();

    if (ipWhitelisting.deWhitelistIp()) {
      return Response.ok().build();
    } else {
      return Response.status(Status.FORBIDDEN)
          .entity("Not whitelisted")
          .type(MediaType.TEXT_PLAIN)
          .build();
    }
  }

  @PUT
  @Timed
  @Produces(MediaType.TEXT_PLAIN)
  @UnitOfWork
  public Response put(@QueryParam("token") int token) {
    if (blacklisting.isBlacklisted())
      return Response.status(Status.TOO_MANY_REQUESTS)
          .entity("Too many requests")
          .type(MediaType.TEXT_PLAIN)
          .build();

    if (!ipWhitelisting.whiteListRequestIp(token)) {
      blacklisting.failure();
      return Response.status(Status.FORBIDDEN)
          .entity("Token does not match")
          .type(MediaType.TEXT_PLAIN)
          .build();
    }
    blacklisting.success();
    return Response.ok().entity("Whitelisting successful").type(MediaType.TEXT_PLAIN).build();
  }

  @GET
  @Timed
  @Produces(MediaType.TEXT_PLAIN)
  @UnitOfWork(readOnly = true)
  public boolean check() {
    if (blacklisting.isBlacklisted()) return false;

    return ipWhitelisting.authoriseIp();
  }
}
