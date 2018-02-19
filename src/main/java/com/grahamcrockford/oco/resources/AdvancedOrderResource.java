package com.grahamcrockford.oco.resources;

import java.math.BigDecimal;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import com.codahale.metrics.annotation.Timed;
import com.grahamcrockford.oco.WebResource;
import com.grahamcrockford.oco.api.TickTrigger;
import com.grahamcrockford.oco.auth.Roles;
import com.grahamcrockford.oco.core.ExchangeService;
import com.grahamcrockford.oco.core.advancedorders.PumpChecker;
import com.grahamcrockford.oco.core.advancedorders.SoftTrailingStop;
import com.grahamcrockford.oco.db.AdvancedOrderAccess;

/**
 * Slightly disorganised endpoint with a mix of methods. Will get re-organised.
 */
@Path("/advancedorders")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AdvancedOrderResource implements WebResource {

  private final ExchangeService exchanges;
  private final AdvancedOrderAccess advancedOrderAccess;

  @Inject
  AdvancedOrderResource(AdvancedOrderAccess advancedOrderAccess,
                        ExchangeService exchanges) {
    this.advancedOrderAccess = advancedOrderAccess;
    this.exchanges = exchanges;
  }

  @DELETE
  @Timed
  @RolesAllowed(Roles.TRADER)
  public void deleteJob() {
    advancedOrderAccess.delete();
  }


  @DELETE
  @Path("{id}")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public void deleteJob(@PathParam("id") String id) {
    advancedOrderAccess.delete(id);
    // TODO shutdown
  }

  @PUT
  @Path("softtrailingstop")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public SoftTrailingStop softTrailingStop(@QueryParam("exchange") String exchange,
                                           @QueryParam("counter") String counter,
                                           @QueryParam("base") String base,
                                           @QueryParam("amount") BigDecimal amount,
                                           @QueryParam("stopPc") BigDecimal stopPercentage,
                                           @QueryParam("limitPc") BigDecimal limitPercentage) throws Exception {

    final Ticker ticker = exchanges.get(exchange).getMarketDataService().getTicker(new CurrencyPair(base, counter));

    SoftTrailingStop stop = advancedOrderAccess.insert(
      SoftTrailingStop.builder()
        .tickTrigger(TickTrigger.builder()
          .exchange(exchange)
          .base(base)
          .counter(counter)
          .build()
        )
        .amount(amount)
        .startPrice(ticker.getBid())
        .stopPercentage(stopPercentage)
        .limitPercentage(limitPercentage)
        .build(),
      SoftTrailingStop.class
    );

    return stop;
  }

  @PUT
  @Path("pumpchecker")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public PumpChecker pumpChecker(@QueryParam("exchange") String exchange,
                                 @QueryParam("counter") String counter,
                                 @QueryParam("base") String base) throws Exception {
    PumpChecker checker = advancedOrderAccess.insert(
      PumpChecker.builder()
        .tickTrigger(TickTrigger.builder()
          .exchange(exchange)
          .base(base)
          .counter(counter)
          .build()
        )
        .build(),
      PumpChecker.class
    );

    return checker;
  }
}