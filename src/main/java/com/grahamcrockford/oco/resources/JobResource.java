package com.grahamcrockford.oco.resources;

import java.math.BigDecimal;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
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
import com.grahamcrockford.oco.api.Job;
import com.grahamcrockford.oco.api.TickerSpec;
import com.grahamcrockford.oco.auth.Roles;
import com.grahamcrockford.oco.core.ExchangeService;
import com.grahamcrockford.oco.core.jobs.OrderStateNotifier;
import com.grahamcrockford.oco.core.jobs.PumpChecker;
import com.grahamcrockford.oco.core.jobs.SoftTrailingStop;
import com.grahamcrockford.oco.db.JobAccess;

/**
 * Slightly disorganised endpoint with a mix of methods. Will get re-organised.
 */
@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class JobResource implements WebResource {

  private final ExchangeService exchanges;
  private final JobAccess advancedOrderAccess;

  @Inject
  JobResource(JobAccess advancedOrderAccess, ExchangeService exchanges) {
    this.advancedOrderAccess = advancedOrderAccess;
    this.exchanges = exchanges;
  }

  @PUT
  @Timed
  @RolesAllowed(Roles.TRADER)
  public Job put(Job job) {
    return advancedOrderAccess.insert(job, Job.class);
  }

  @DELETE
  @Timed
  @RolesAllowed(Roles.TRADER)
  public void deleteAllJobs() {
    advancedOrderAccess.delete();
  }


  @DELETE
  @Path("{id}")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public void deleteJob(@PathParam("id") String id) {
    advancedOrderAccess.delete(id);
  }

  @PUT
  @Path("softtrailingstop")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public SoftTrailingStop softTrailingStop(@QueryParam("exchange") String exchange,
                                           @QueryParam("counter") String counter,
                                           @QueryParam("base") String base,
                                           @QueryParam("amount") BigDecimal amount,
                                           @QueryParam("stop") BigDecimal stopPrice,
                                           @QueryParam("limit") BigDecimal limitPrice) throws Exception {

    final Ticker ticker = exchanges.get(exchange).getMarketDataService().getTicker(new CurrencyPair(base, counter));

    SoftTrailingStop job = SoftTrailingStop.builder()
        .tickTrigger(TickerSpec.builder()
          .exchange(exchange)
          .base(base)
          .counter(counter)
          .build()
        )
        .amount(amount)
        .startPrice(ticker.getBid())
        .stopPrice(stopPrice)
        .limitPrice(limitPrice)
        .build();

    advancedOrderAccess.insert(job, SoftTrailingStop.class);

    return job;
  }

  @PUT
  @Path("pumpchecker")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public PumpChecker pumpChecker(@QueryParam("exchange") String exchange,
                                 @QueryParam("counter") String counter,
                                 @QueryParam("base") String base) throws Exception {

    // Just check it's a valid ticker
    exchanges.get(exchange).getMarketDataService().getTicker(new CurrencyPair(base, counter));

    PumpChecker job = PumpChecker.builder()
        .tickTrigger(TickerSpec.builder()
          .exchange(exchange)
          .base(base)
          .counter(counter)
          .build()
        )
        .build();

    advancedOrderAccess.insert(job, PumpChecker.class);

    return job;
  }

  @PUT
  @Path("monitororder")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public OrderStateNotifier monitorOrder(@QueryParam("exchange") String exchange,
                                         @QueryParam("orderId") String orderId) throws Exception {

    OrderStateNotifier job = OrderStateNotifier.builder()
        .exchange(exchange)
        .description("Web request")
        .orderId(orderId)
        .build();

    advancedOrderAccess.insert(job, OrderStateNotifier.class);

    return job;
  }
}