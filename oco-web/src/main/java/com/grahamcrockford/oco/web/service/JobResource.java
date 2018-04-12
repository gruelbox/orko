package com.grahamcrockford.oco.web.service;

import java.io.IOException;
import java.math.BigDecimal;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import com.grahamcrockford.oco.api.auth.Roles;
import com.grahamcrockford.oco.api.exchange.ExchangeService;
import com.grahamcrockford.oco.api.job.LimitOrderJob;
import com.grahamcrockford.oco.api.job.LimitOrderJob.Direction;
import com.grahamcrockford.oco.api.job.OrderStateNotifier;
import com.grahamcrockford.oco.api.job.PumpChecker;
import com.grahamcrockford.oco.api.job.SoftTrailingStop;
import com.grahamcrockford.oco.api.process.JobAccess;
import com.grahamcrockford.oco.api.process.JobAccess.JobDoesNotExistException;
import com.grahamcrockford.oco.api.process.JobSubmitter;
import com.grahamcrockford.oco.spi.Job;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.web.WebResource;

import io.dropwizard.auth.AuthenticationException;

/**
 * Slightly disorganised endpoint with a mix of methods. Will get re-organised.
 */
@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class JobResource implements WebResource {

  private final ExchangeService exchanges;
  private final JobSubmitter jobSubmitter;
  private final JobAccess jobAccess;

  @Inject
  JobResource(JobAccess jobAccess, JobSubmitter jobSubmitter, ExchangeService exchanges) {
    this.jobAccess = jobAccess;
    this.jobSubmitter = jobSubmitter;
    this.exchanges = exchanges;
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
    return jobSubmitter.submitNewUnchecked(job);
  }

  @DELETE
  @Timed
  @RolesAllowed(Roles.TRADER)
  public void deleteAllJobs() throws AuthenticationException {
    jobAccess.delete();
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
    jobAccess.delete(id);
  }

  @PUT
  @Path("softtrailingstop")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public SoftTrailingStop softTrailingStop(@QueryParam("exchange") String exchange,
                                           @QueryParam("counter") String counter,
                                           @QueryParam("base") String base,
                                           @QueryParam("direction") Direction direction,
                                           @QueryParam("amount") BigDecimal amount,
                                           @QueryParam("stop") BigDecimal stopPrice,
                                           @QueryParam("limit") BigDecimal limitPrice) throws IOException {

    final Ticker ticker = exchanges.get(exchange).getMarketDataService().getTicker(new CurrencyPair(base, counter));

   return jobSubmitter.submitNewUnchecked(SoftTrailingStop.builder()
        .tickTrigger(TickerSpec.builder()
          .exchange(exchange)
          .base(base)
          .counter(counter)
          .build()
        )
        .direction(direction)
        .amount(amount)
        .startPrice(ticker.getBid())
        .stopPrice(stopPrice)
        .limitPrice(limitPrice)
        .build());
  }

  @PUT
  @Path("pumpchecker")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public PumpChecker pumpChecker(@QueryParam("exchange") String exchange,
                                 @QueryParam("counter") String counter,
                                 @QueryParam("base") String base) throws IOException {

    // Just check it's a valid ticker
    exchanges.get(exchange).getMarketDataService().getTicker(new CurrencyPair(base, counter));

    return jobSubmitter.submitNewUnchecked(PumpChecker.builder()
        .tickTrigger(TickerSpec.builder()
          .exchange(exchange)
          .base(base)
          .counter(counter)
          .build()
        )
        .build());
  }

  @PUT
  @Path("monitororder")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public OrderStateNotifier monitorOrder(@QueryParam("exchange") String exchange,
                                         @QueryParam("counter") String counter,
                                         @QueryParam("base") String base,
                                         @QueryParam("orderId") String orderId) {

    return jobSubmitter.submitNewUnchecked(OrderStateNotifier.builder()
        .tickTrigger(TickerSpec.builder()
          .exchange(exchange)
          .base(base)
          .counter(counter)
          .build())
        .orderId(orderId)
        .build());
  }

  @PUT
  @Path("limitbuy")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public LimitOrderJob limitBuy(@QueryParam("exchange") String exchange,
                                 @QueryParam("counter") String counter,
                                 @QueryParam("base") String base,
                                 @QueryParam("amount") BigDecimal amount,
                                 @QueryParam("limit") BigDecimal limitPrice) throws IOException {
    return limitOrder(exchange, counter, base, amount, limitPrice, Direction.BUY);
  }

  @PUT
  @Path("limitsell")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public LimitOrderJob limitSell(@QueryParam("exchange") String exchange,
                                 @QueryParam("counter") String counter,
                                 @QueryParam("base") String base,
                                 @QueryParam("amount") BigDecimal amount,
                                 @QueryParam("limit") BigDecimal limitPrice) throws IOException {
    return limitOrder(exchange, counter, base, amount, limitPrice, Direction.SELL);
  }

  private LimitOrderJob limitOrder(String exchange, String counter, String base, BigDecimal amount,
      BigDecimal limitPrice, Direction direction) throws IOException {
    // Just check it's a valid ticker
    exchanges.get(exchange).getMarketDataService().getTicker(new CurrencyPair(base, counter));

    return jobSubmitter.submitNewUnchecked(LimitOrderJob.builder()
        .tickTrigger(TickerSpec.builder()
            .exchange(exchange)
            .base(base)
            .counter(counter)
            .build()
          )
        .direction(direction)
        .amount(amount)
        .limitPrice(limitPrice)
        .build());
  }
}