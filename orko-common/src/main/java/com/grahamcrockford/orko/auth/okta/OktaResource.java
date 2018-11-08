package com.grahamcrockford.orko.auth.okta;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.wiring.WebResource;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class OktaResource implements WebResource {

  private final AuthConfiguration authConfiguration;

  @Inject
  OktaResource(AuthConfiguration authConfiguration) {
    this.authConfiguration = authConfiguration;
  }

  @GET
  @Path("/config")
  @Timed
  public OktaConfiguration getConfig() {
    return authConfiguration.getOkta();
  }
}