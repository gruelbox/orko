package com.grahamcrockford.oco.marketdata;

import static com.grahamcrockford.oco.marketdata.MarketDataType.ORDERBOOK;
import static com.grahamcrockford.oco.marketdata.MarketDataType.TICKER;

import java.util.Collection;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.FluentIterable;
import com.grahamcrockford.oco.auth.Roles;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.wiring.WebResource;

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
    return FluentIterable.from(permanentSubscriptionManager.all())
        .filter(s -> s.type().equals(TICKER))
        .transform(s -> s.spec())
        .toSet();
  }

  @PUT
  @Timed
  @RolesAllowed(Roles.TRADER)
  public void put(TickerSpec spec) {
    permanentSubscriptionManager.add(
      MarketDataSubscription.create(spec, TICKER),
      MarketDataSubscription.create(spec, ORDERBOOK)
    );
  }

  @DELETE
  @Timed
  @RolesAllowed(Roles.TRADER)
  public void delete(TickerSpec spec) {
    permanentSubscriptionManager.remove(
      MarketDataSubscription.create(spec, TICKER),
      MarketDataSubscription.create(spec, ORDERBOOK)
    );
  }
}