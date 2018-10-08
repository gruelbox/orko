package com.grahamcrockford.oco.exchange;

import java.util.Collection;

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

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import com.grahamcrockford.oco.auth.Roles;
import com.grahamcrockford.oco.notification.NotificationService;
import com.grahamcrockford.oco.spi.Job;
import com.grahamcrockford.oco.submit.JobAccess;
import com.grahamcrockford.oco.submit.JobAccess.JobDoesNotExistException;
import com.grahamcrockford.oco.submit.JobSubmitter;
import com.grahamcrockford.oco.wiring.WebResource;

import io.dropwizard.auth.AuthenticationException;

/**
 * Slightly disorganised endpoint with a mix of methods. Will get re-organised.
 */
@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class JobResource implements WebResource {

  private final JobSubmitter jobSubmitter;
  private final JobAccess jobAccess;
  private final NotificationService notificationService;

  @Inject
  JobResource(JobAccess jobAccess, JobSubmitter jobSubmitter, NotificationService notificationService) {
    this.jobAccess = jobAccess;
    this.jobSubmitter = jobSubmitter;
    this.notificationService = notificationService;
  }

  @GET
  @Timed
  @RolesAllowed(Roles.TRADER)
  public Collection<Job> list() throws AuthenticationException {
    return ImmutableList.copyOf(jobAccess.list());
  }

  @PUT
  @Timed
  @RolesAllowed(Roles.TRADER)
  public Job put(Job job) throws AuthenticationException {
    Job created = jobSubmitter.submitNewUnchecked(job);
    notificationService.info("Requested: " + created + " (" + created.id() + ")");
    return created;
  }

  @DELETE
  @Timed
  @RolesAllowed(Roles.TRADER)
  public void deleteAllJobs() throws AuthenticationException {
    jobAccess.deleteAll();
  }

  @GET
  @Path("{id}")
  @Timed
  @RolesAllowed(Roles.TRADER)
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
  @RolesAllowed(Roles.TRADER)
  public void deleteJob(@PathParam("id") String id) {
    Job job = jobAccess.load(id);
    jobAccess.delete(id);
    notificationService.info("Deleted: " + job + " (" + id + ")");
  }
}