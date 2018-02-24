package com.grahamcrockford.oco.resources;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.grahamcrockford.oco.WebResource;
import com.grahamcrockford.oco.auth.IpWhitelisting;

/**
 * Allows an IP to be whitelisted.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AuthResource implements WebResource {

  private final IpWhitelisting ipWhitelisting;

  @Inject
  AuthResource(IpWhitelisting ipWhitelisting) {
    this.ipWhitelisting = ipWhitelisting;
  }

  @PUT
  @Timed
  public Response auth(@QueryParam("token") int token) {
    if (!ipWhitelisting.whiteListRequestIp(token)) {
      return Response.status(401).entity("Not authorised").type(MediaType.TEXT_PLAIN).build();
    }
    return Response.ok().entity("IP address has been whitelisted").type(MediaType.TEXT_PLAIN).build();
  }
}