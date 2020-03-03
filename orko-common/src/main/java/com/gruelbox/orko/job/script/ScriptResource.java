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
package com.gruelbox.orko.job.script;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.gruelbox.orko.auth.Hasher;
import com.gruelbox.orko.jobrun.JobResource;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import io.dropwizard.hibernate.UnitOfWork;
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

/** Submission and management of scripts and script jobs. */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class ScriptResource implements WebResource {

  private final JobResource jobResource;
  private final Hasher hasher;
  private final ScriptConfiguration config;
  private final ScriptAccess scriptAccess;

  @Inject
  ScriptResource(
      JobResource jobResource,
      ScriptAccess scriptAccess,
      ScriptConfiguration configuration,
      Hasher hasher) {
    this.jobResource = jobResource;
    this.scriptAccess = scriptAccess;
    this.config = configuration;
    this.hasher = hasher;
  }

  @PUT
  @Timed
  @Path("/scripts/{id}")
  @UnitOfWork
  public Response putScript(@PathParam("id") String id, Script script) {
    if (!id.equals(script.id()))
      return Response.status(400)
          .entity(ImmutableMap.of("error", "id doesn't match endpoint"))
          .build();
    scriptAccess.saveOrUpdate(script);
    return Response.ok().build();
  }

  @DELETE
  @Timed
  @Path("/scripts/{id}")
  @UnitOfWork
  public void deleteScript(@PathParam("id") String id) {
    scriptAccess.delete(id);
  }

  @GET
  @Timed
  @Path("/scripts")
  @UnitOfWork(readOnly = true)
  public Iterable<Script> listScripts() {
    return scriptAccess.list();
  }

  @PUT
  @Timed
  @Path("/scriptjobs/{id}")
  @UnitOfWork
  public Response putJob(@PathParam("id") String id, ScriptJobPrototype job) {
    return jobResource.put(
        id,
        ScriptJob.builder()
            .id(job.id)
            .name(job.name)
            .script(job.script)
            .scriptHash(
                StringUtils.isNotEmpty(config.getScriptSigningKey())
                    ? hasher.hashWithString(job.script, config.getScriptSigningKey())
                    : ScriptAccess.UNSIGNED)
            .build());
  }

  /**
   * A partially defined script sent by the client.
   *
   * @author Graham Crockford
   */
  public static final class ScriptJobPrototype {

    @JsonProperty private String id;
    @JsonProperty private String name;
    @JsonProperty private String script;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getScript() {
      return script;
    }

    public void setScript(String script) {
      this.script = script;
    }
  }
}
