package com.gruelbox.orko.support;

import javax.annotation.security.RolesAllowed;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.MoreObjects;
import com.gruelbox.orko.auth.Roles;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

@Path("/support")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class SupportResource implements WebResource {

  @GET
  @Path("/meta")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public SupportMetadata getMeta() {
    return SupportMetadata.create(MoreObjects.firstNonNull(ReadVersion.readVersionInfoInManifest(), "Development build"));
  }
}