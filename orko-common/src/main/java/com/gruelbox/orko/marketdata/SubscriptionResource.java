package com.gruelbox.orko.marketdata;

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

/**
 * Access to exchange information.
 */
@Path("/subscriptions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class SubscriptionResource implements WebResource {

  private final PermanentSubscriptionManager permanentSubscriptionManager;

  @Inject
  SubscriptionResource(PermanentSubscriptionManager permanentSubscriptionManager) {
    this.permanentSubscriptionManager = permanentSubscriptionManager;
  }


  @GET
  @Timed
  @RolesAllowed(Roles.TRADER)
  public Collection<TickerSpec> list() {
    return permanentSubscriptionManager.all();
  }

  @PUT
  @Timed
  @RolesAllowed(Roles.TRADER)
  public void put(TickerSpec spec) {
    permanentSubscriptionManager.add(spec);
  }

  @GET
  @Timed
  @Path("referencePrices")
  @RolesAllowed(Roles.TRADER)
  public Map<String, BigDecimal> listReferencePrices() {
    Map<String, Entry<TickerSpec, BigDecimal>> rekeyed = Maps.uniqueIndex(permanentSubscriptionManager.referencePrices().entrySet(), e -> e.getKey().key());
    Map<String, BigDecimal> result = Maps.transformValues(rekeyed, e -> e.getValue());
    return result;
  }

  @PUT
  @Timed
  @Path("referencePrices/{exchange}/{base}-{counter}")
  @RolesAllowed(Roles.TRADER)
  public void setReferencePrice(@PathParam("exchange") String exchange,
                                @PathParam("counter") String counter,
                                @PathParam("base") String base,
                                BigDecimal price) {
    permanentSubscriptionManager.setReferencePrice(TickerSpec.builder().exchange(exchange).base(base).counter(counter).build(), price);
  }

  @DELETE
  @Timed
  @RolesAllowed(Roles.TRADER)
  public void delete(TickerSpec spec) {
    permanentSubscriptionManager.remove(spec);
  }
}