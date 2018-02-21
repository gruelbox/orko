package com.grahamcrockford.oco.resources;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeResource.class);

  private final ExchangeService exchanges;

  @Inject
  ExchangeResource(ExchangeService exchanges) {
    this.exchanges = exchanges;
  }

  @GET
  @Timed
  @RolesAllowed(Roles.PUBLIC)
  public Collection<String> list() {
    return exchanges.getExchanges();
  }

  @GET
  @Path("{exchange}/orders")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public OpenOrders orders(@PathParam("exchange") String exchange) throws IOException {
    return exchanges.get(exchange).getTradeService().getOpenOrders();
  }

  @GET
  @Path("{exchange}/currencies/{currency}/orders")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public List<Order> orders(@PathParam("exchange") String exchangeCode,
                            @PathParam("currency") String currency) throws IOException {
    LOGGER.info("Thorough orders search...");
    Exchange exchange = exchanges.get(exchangeCode);
    return exchange
      .getExchangeMetaData()
      .getCurrencyPairs()
      .keySet()
      .stream()
      .filter(p -> p.base.getCurrencyCode().equals(currency) || p.counter.getCurrencyCode().equals(currency))
      .peek(p -> LOGGER.info("Checking " + p))
      .flatMap(p -> {
        try {
          Thread.sleep(200);
          return exchange
            .getTradeService()
            .getOpenOrders(new DefaultOpenOrdersParamCurrencyPair(p))
            .getOpenOrders()
            .stream();
        } catch (IOException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      })
      .collect(Collectors.toList());
  }

  @GET
  @Path("{exchange}/markets/{base}-{counter}/orders")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public OpenOrders orders(@PathParam("exchange") String exchange,
                       @PathParam("counter") String counter,
                       @PathParam("base") String base) throws IOException {
    return exchanges.get(exchange)
        .getTradeService()
        .getOpenOrders(new DefaultOpenOrdersParamCurrencyPair(new CurrencyPair(base, counter)));
  }

  @GET
  @Path("{exchange}/orders/{id}")
  @Timed
  @RolesAllowed(Roles.PUBLIC)
  public Collection<Order> order(@PathParam("exchange") String exchange, @PathParam("id") String id) throws IOException {
    return exchanges.get(exchange)
        .getTradeService()
        .getOrder(id);
  }

  @GET
  @Path("{exchange}/markets/{base}-{counter}/ticker")
  @Timed
  @RolesAllowed(Roles.PUBLIC)
  public Ticker ticker(@PathParam("exchange") String exchange,
                       @PathParam("counter") String counter,
                       @PathParam("base") String base) throws IOException {
    return exchanges.get(exchange)
        .getMarketDataService()
        .getTicker(new CurrencyPair(base, counter));
  }
}