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
