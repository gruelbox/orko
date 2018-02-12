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
import com.grahamcrockford.oco.api.AdvancedOrderInfo;
import com.grahamcrockford.oco.auth.Roles;
import com.grahamcrockford.oco.core.ExchangeService;
import com.grahamcrockford.oco.core.advancedorders.PumpChecker;
import com.grahamcrockford.oco.core.advancedorders.SoftTrailingStop;
import com.grahamcrockford.oco.core.AdvancedOrderEnqueuer;
import com.grahamcrockford.oco.core.AdvancedOrderListener;

/**
 * Slightly disorganised endpoint with a mix of methods. Will get re-organised.
 */
@Path("/advancedorders")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AdvancedOrderResource implements WebResource {

  private final ExchangeService exchanges;
  private final AdvancedOrderEnqueuer advancedOrderEnqueuer;
  private final AdvancedOrderListener advancedOrderListener;

  @Inject
  AdvancedOrderResource(AdvancedOrderEnqueuer advancedOrderEnqueuer,
                        AdvancedOrderListener advancedOrderListener,
                        ExchangeService exchanges) {
    this.advancedOrderEnqueuer = advancedOrderEnqueuer;
    this.advancedOrderListener = advancedOrderListener;
    this.exchanges = exchanges;
  }

  @DELETE
  @Path("{id}")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public void deleteJob(@PathParam("id") long id) {
    advancedOrderListener.delete(id);
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
    return advancedOrderEnqueuer.enqueue(
      SoftTrailingStop.builder()
        .basic(AdvancedOrderInfo.builder()
          .exchange(exchange)
          .base(base)
          .counter(counter)
          .build()
        )
        .amount(amount)
        .startPrice(ticker.getBid())
        .stopPercentage(stopPercentage)
        .limitPercentage(limitPercentage)
        .build()
    );
  }

  @PUT
  @Path("pumpchecker")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public PumpChecker pumpChecker(@QueryParam("exchange") String exchange,
                                 @QueryParam("counter") String counter,
                                 @QueryParam("base") String base) throws Exception {
    return advancedOrderEnqueuer.enqueue(
      PumpChecker.builder()
        .basic(AdvancedOrderInfo.builder()
          .exchange(exchange)
          .base(base)
          .counter(counter)
          .build()
        )
        .build()
    );
  }
}