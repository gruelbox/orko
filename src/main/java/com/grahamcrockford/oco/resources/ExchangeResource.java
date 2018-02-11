package com.grahamcrockford.oco.resources;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.OpenOrders;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import com.grahamcrockford.oco.WebResource;
import com.grahamcrockford.oco.auth.Roles;
import com.grahamcrockford.oco.core.ExchangeService;

/**
 * Access to exchange information.
 */
@Path("/exchanges")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ExchangeResource implements WebResource {

  private final ExchangeService exchanges;

  @Inject
  ExchangeResource(ExchangeService exchanges) {
    this.exchanges = exchanges;
  }

  @GET
  @Timed
  @RolesAllowed(Roles.PUBLIC)
  public List<String> list() {
    return ImmutableList.of("gdax-sandbox", "binance");
  }

  @GET
  @Path("{exchange}/orders")
  @Timed
  @RolesAllowed(Roles.PUBLIC)
  public OpenOrders orders(@PathParam("exchange") String exchange) throws IOException {
    return exchanges.get(exchange).getTradeService().getOpenOrders();
  }

  @GET
  @Path("{exchange}/orders/{id}")
  @Timed
  @RolesAllowed(Roles.PUBLIC)
  public Collection<Order> order(@PathParam("exchange") String exchange, @PathParam("id") String id) throws IOException {
    return exchanges.get(exchange).getTradeService().getOrder(id);
  }

  @GET
  @Path("{exchange}/markets/{base}-{counter}/ticker")
  @Timed
  @RolesAllowed(Roles.PUBLIC)
  public Ticker ticker(@PathParam("exchange") String exchange,
                       @PathParam("counter") String counter,
                       @PathParam("base") String base) throws IOException {
    return exchanges.get(exchange).getMarketDataService().getTicker(new CurrencyPair(base, counter));
  }
}