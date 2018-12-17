package com.gruelbox.orko.job;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.auth.Hasher;
import com.gruelbox.orko.auth.Roles;
import com.gruelbox.orko.jobrun.JobResource;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.hibernate.UnitOfWork;

/**
 * Submission and management of script jobs.
 */
@Path("/jobs/scripts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class ScriptResource implements WebResource {

  private final JobResource jobResource;
  private final Hasher hasher;
  private final OrkoConfiguration configuration;

  @Inject
  ScriptResource(JobResource jobResource, OrkoConfiguration configuration, Hasher hasher) {
    this.jobResource = jobResource;
    this.configuration = configuration;
    this.hasher = hasher;
  }

  @PUT
  @Timed
  @Path("/{id}")
  @UnitOfWork
  @RolesAllowed(Roles.TRADER)
  public Response put(@PathParam("id") String id, ScriptJobPrototype job) throws AuthenticationException {
    return jobResource.put(id, ScriptJob.builder()
        .id(job.id)
        .name(job.name)
        .script(job.script)
        .scriptHash(hasher.hashWithString(job.script, configuration.getScriptSigningKey()))
        .build());
  }

  public static final class ScriptJobPrototype {
    public String id;
    public String name;
    public String script;
  }
}