package com.gruelbox.orko.auth.ipwhitelisting;

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
import com.gruelbox.orko.auth.blacklist.Blacklisting;
import com.gruelbox.orko.wiring.WebResource;

@Path("/auth")
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
  @Produces(MediaType.TEXT_PLAIN)
  public Response auth() {
    if (blacklisting.isBlacklisted())
      return Response.status(Status.TOO_MANY_REQUESTS).entity("Too many requests").type(MediaType.TEXT_PLAIN).build();
      
    if (ipWhitelisting.deWhitelistIp()) {
      return Response.ok().build();
    } else {
      return Response.status(Status.FORBIDDEN).entity("Not whitelisted").type(MediaType.TEXT_PLAIN).build();
    }
  }

  @PUT
  @Timed
  @Produces(MediaType.TEXT_PLAIN)
  public Response auth(@QueryParam("token") int token) {
    if (blacklisting.isBlacklisted())
      return Response.status(Status.TOO_MANY_REQUESTS).entity("Too many requests").type(MediaType.TEXT_PLAIN).build();
    
    if (!ipWhitelisting.whiteListRequestIp(token)) {
      blacklisting.failure();
      return Response.status(Status.FORBIDDEN).entity("Token does not match").type(MediaType.TEXT_PLAIN).build();
    }
    blacklisting.success();
    return Response.ok().entity("Whitelisting successful").type(MediaType.TEXT_PLAIN).build();
  }

  @GET
  @Timed
  @Produces(MediaType.TEXT_PLAIN)
  public boolean check() {
    if (blacklisting.isBlacklisted())
      return false;
    
    return ipWhitelisting.authoriseIp();
  }
}