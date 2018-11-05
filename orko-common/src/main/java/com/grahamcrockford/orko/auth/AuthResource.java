package com.grahamcrockford.orko.auth;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.grahamcrockford.orko.wiring.WebResource;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AuthResource implements WebResource {

  private final AuthConfiguration authConfiguration;

  @Inject
  AuthResource(AuthConfiguration authConfiguration) {
    this.authConfiguration = authConfiguration;
  }

  @GET
  @Path("/config")
  @Timed
  public OktaConfiguration getConfig() {
    return authConfiguration.getOkta() == null ? new OktaConfiguration() : authConfiguration.getOkta();
  }
}