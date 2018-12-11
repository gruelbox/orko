package com.gruelbox.orko.subscription;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

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

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Maps;
import com.gruelbox.orko.auth.Roles;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

import io.dropwizard.hibernate.UnitOfWork;

/**
 * Access to exchange information.
 */
@Path("/subscriptions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class SubscriptionResource implements WebResource {

  private final SubscriptionManager subscriptionManager;
  private final SubscriptionAccess subscriptionAccess;

  @Inject
  SubscriptionResource(SubscriptionManager permanentSubscriptionManager,
                       SubscriptionAccess permanentSubscriptionAccess) {
    this.subscriptionManager = permanentSubscriptionManager;
    this.subscriptionAccess = permanentSubscriptionAccess;
  }


  @GET
  @Timed
  @UnitOfWork(readOnly = true)
  @RolesAllowed(Roles.TRADER)
  public Collection<TickerSpec> list() {
    return subscriptionAccess.all();
  }

  @PUT
  @Timed
  @UnitOfWork
  @RolesAllowed(Roles.TRADER)
  public void put(TickerSpec spec) {
    subscriptionManager.add(spec);
  }

  @GET
  @Timed
  @UnitOfWork(readOnly = true)
  @Path("referencePrices")
  @RolesAllowed(Roles.TRADER)
  public Map<String, BigDecimal> listReferencePrices() {
    Map<String, Entry<TickerSpec, BigDecimal>> rekeyed = Maps.uniqueIndex(subscriptionAccess.getReferencePrices().entrySet(), e -> e.getKey().key());
    Map<String, BigDecimal> result = Maps.transformValues(rekeyed, e -> e.getValue());
    return result;
  }

  @PUT
  @Timed
  @UnitOfWork
  @Path("referencePrices/{exchange}/{base}-{counter}")
  @RolesAllowed(Roles.TRADER)
  public void setReferencePrice(@PathParam("exchange") String exchange,
                                @PathParam("counter") String counter,
                                @PathParam("base") String base,
                                BigDecimal price) {
    subscriptionAccess.setReferencePrice(TickerSpec.builder().exchange(exchange).base(base).counter(counter).build(), price);
  }

  @DELETE
  @Timed
  @UnitOfWork
  @RolesAllowed(Roles.TRADER)
  public void delete(TickerSpec spec) {
    subscriptionManager.remove(spec);
  }
}