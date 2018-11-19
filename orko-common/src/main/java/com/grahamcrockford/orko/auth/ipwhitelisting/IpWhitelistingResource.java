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
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.annotation.Timed;
import com.grahamcrockford.orko.auth.blacklist.Blacklisting;
import com.grahamcrockford.orko.wiring.WebResource;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IpWhitelistingResource implements WebResource {

  private final IpWhitelisting ipWhitelisting;
  private final Blacklisting blacklisting;

  @Inject
  IpWhitelistingResource(IpWhitelisting ipWhitelisting, Blacklisting blacklisting) {
    this.ipWhitelisting = ipWhitelisting;
    this.blacklisting = blacklisting;
  }

  @DELETE
  @Timed
  public Response auth() {
    if (blacklisting.isBlacklisted())
      return Response.status(Status.TOO_MANY_REQUESTS).type(MediaType.TEXT_PLAIN).build();
      
    if (ipWhitelisting.deWhitelistIp()) {
      return Response.ok().build();
    } else {
      return Response.status(Status.FORBIDDEN).entity("Not whitelisted").type(MediaType.TEXT_PLAIN).build();
    }
  }

  @PUT
  @Timed
  public Response auth(@QueryParam("token") int token) {
    if (blacklisting.isBlacklisted())
      return Response.status(Status.TOO_MANY_REQUESTS).type(MediaType.TEXT_PLAIN).build();
    
    if (!ipWhitelisting.whiteListRequestIp(token)) {
      blacklisting.failure();
      return Response.status(Status.FORBIDDEN).entity("Invalid").type(MediaType.TEXT_PLAIN).build();
    }
    blacklisting.success();
    return Response.ok().entity("Whitelisting successful").type(MediaType.TEXT_PLAIN).build();
  }

  @GET
  @Timed
  public boolean check() {
    if (blacklisting.isBlacklisted())
      return false;
    
    return ipWhitelisting.authoriseIp();
  }
}