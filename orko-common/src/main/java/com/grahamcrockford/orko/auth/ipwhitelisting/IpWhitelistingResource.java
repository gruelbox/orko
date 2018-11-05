package com.grahamcrockford.orko.auth.ipwhitelisting;

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

import com.codahale.metrics.annotation.Timed;
import com.grahamcrockford.orko.auth.ipwhitelisting.IpWhitelisting;
import com.grahamcrockford.orko.wiring.WebResource;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IpWhitelistingResource implements WebResource {

  private final IpWhitelisting ipWhitelisting;

  @Inject
  IpWhitelistingResource(IpWhitelisting ipWhitelisting) {
    this.ipWhitelisting = ipWhitelisting;
  }

  @DELETE
  @Timed
  public Response auth() {
    if (ipWhitelisting.deWhitelistIp()) {
      return Response.ok().build();
    } else {
      return Response.status(403).entity("Not whitelisted").type(MediaType.TEXT_PLAIN).build();
    }
  }

  @PUT
  @Timed
  public Response auth(@QueryParam("token") int token) {
    if (!ipWhitelisting.whiteListRequestIp(token)) {
      return Response.status(403).entity("Invalid").type(MediaType.TEXT_PLAIN).build();
    }
    return Response.ok().entity("Whitelisting successful").type(MediaType.TEXT_PLAIN).build();
  }

  @GET
  @Timed
  public boolean check() {
    return ipWhitelisting.authoriseIp();
  }
}