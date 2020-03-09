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
package com.gruelbox.orko.jobrun;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.gruelbox.orko.jobrun.JobAccess.JobDoesNotExistException;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import io.dropwizard.hibernate.UnitOfWork;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;

/** Submission and management of jobs. */
@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class JobResource implements WebResource {

  private final JobSubmitter jobSubmitter;
  private final JobAccess jobAccess;

  @Inject
  JobResource(JobAccess jobAccess, JobSubmitter jobSubmitter) {
    this.jobAccess = jobAccess;
    this.jobSubmitter = jobSubmitter;
  }

  @GET
  @Timed
  @UnitOfWork(readOnly = true)
  public Collection<Job> list() {
    return ImmutableList.copyOf(jobAccess.list());
  }

  @PUT
  @Timed
  @Path("/{id}")
  @UnitOfWork
  public Response put(@PathParam("id") String id, Job job) {
    if (StringUtils.isEmpty(job.id()) || !job.id().equals(id))
      return Response.status(400)
          .entity(ImmutableMap.of("error", "id not set or query and body do not match"))
          .build();
    jobSubmitter.submitNewUnchecked(job);
    return Response.ok().build();
  }

  @POST
  @Timed
  @UnitOfWork
  public Job post(Job job) {
    return jobSubmitter.submitNewUnchecked(job);
  }

  @DELETE
  @Timed
  @UnitOfWork
  public void deleteAllJobs() {
    jobAccess.deleteAll();
  }

  @GET
  @Path("{id}")
  @Timed
  @UnitOfWork(readOnly = true)
  public Response fetchJob(@PathParam("id") String id) {
    try {
      return Response.ok().entity(jobAccess.load(id)).build();
    } catch (JobDoesNotExistException e) {
      return Response.status(404).build();
    }
  }

  @DELETE
  @Path("{id}")
  @Timed
  @UnitOfWork
  public void deleteJob(@PathParam("id") String id) {
    jobAccess.delete(id);
  }
}
