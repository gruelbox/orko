/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gruelbox.orko.job.script;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.auth.Hasher;
import com.gruelbox.orko.auth.Roles;
import com.gruelbox.orko.jobrun.JobResource;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.hibernate.UnitOfWork;

/**
 * Submission and management of scripts and script jobs.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class ScriptResource implements WebResource {

  private final JobResource jobResource;
  private final Hasher hasher;
  private final OrkoConfiguration config;
  private final ScriptAccess scriptAccess;

  @Inject
  ScriptResource(JobResource jobResource, ScriptAccess scriptAccess, OrkoConfiguration configuration, Hasher hasher) {
    this.jobResource = jobResource;
    this.scriptAccess = scriptAccess;
    this.config = configuration;
    this.hasher = hasher;
  }

  @PUT
  @Timed
  @Path("/scripts/{id}")
  @UnitOfWork
  @RolesAllowed(Roles.TRADER)
  public Response putScript(@PathParam("id") String id, Script script) throws AuthenticationException {
    if (!id.equals(script.id()))
      return Response.status(400).entity(ImmutableMap.of("error", "id doesn't match endpoint")).build();
    scriptAccess.saveOrUpdate(script);
    return Response.ok().build();
  }

  @DELETE
  @Timed
  @Path("/scripts/{id}")
  @UnitOfWork
  @RolesAllowed(Roles.TRADER)
  public void deleteScript(@PathParam("id") String id) throws AuthenticationException {
    scriptAccess.delete(id);
  }

  @GET
  @Timed
  @Path("/scripts")
  @UnitOfWork(readOnly = true)
  @RolesAllowed(Roles.TRADER)
  public Iterable<Script> listScripts() throws AuthenticationException {
    return scriptAccess.list();
  }

  @PUT
  @Timed
  @Path("/scriptjobs/{id}")
  @UnitOfWork
  @RolesAllowed(Roles.TRADER)
  public Response putJob(@PathParam("id") String id, ScriptJobPrototype job) throws AuthenticationException {
    return jobResource.put(id, ScriptJob.builder()
        .id(job.id)
        .name(job.name)
        .script(job.script)
        .scriptHash(StringUtils.isNotEmpty(config.getScriptSigningKey())
            ? hasher.hashWithString(job.script, config.getScriptSigningKey())
            : ScriptAccess.UNSIGNED)
        .build());
  }

  public static final class ScriptJobPrototype {
    public String id;
    public String name;
    public String script;
  }
}
