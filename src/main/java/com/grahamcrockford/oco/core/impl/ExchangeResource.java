package com.grahamcrockford.oco.core.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.grahamcrockford.oco.WebResource;
import com.grahamcrockford.oco.auth.Roles;
import com.grahamcrockford.oco.core.api.ExchangeService;

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
  @Timed
  @Path("{exchange}/counters")
  @RolesAllowed(Roles.PUBLIC)
  public Collection<String> counters(@PathParam("exchange") String exchange) {
    return exchanges.get(exchange)
        .getExchangeMetaData()
        .getCurrencyPairs()
        .keySet()
        .stream()
        .map(currencyPair -> currencyPair.counter.getCurrencyCode())
        .collect(Collectors.toSet());
  }

  @GET
  @Timed
  @Path("{exchange}/counters/{counter}/bases")
  @RolesAllowed(Roles.PUBLIC)
  public Collection<String> bases(@PathParam("exchange") String exchange,
                                  @PathParam("counter") String counter) {
    return exchanges.get(exchange)
        .getExchangeMetaData()
        .getCurrencyPairs()
        .keySet()
        .stream()
        .filter(currencyPair -> currencyPair.counter.getCurrencyCode().equals(counter))
        .map(currencyPair -> currencyPair.base.getCurrencyCode())
        .collect(Collectors.toSet());
  }

  @GET
  @Path("{exchange}/orders")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public OpenOrders orders(@PathParam("exchange") String exchange) throws IOException {
    return exchanges.get(exchange)
        .getTradeService()
        .getOpenOrders();
  }

  @GET
  @Path("{exchange}/balance/{currencies}")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public Map<String, Balance> balances(@PathParam("exchange") String exchange, @PathParam("currencies") String currenciesAsString) throws IOException {

    Set<String> currencies = Stream.of(currenciesAsString.split(","))
        .collect(Collectors.toSet());

    FluentIterable<Balance> balances = FluentIterable.from(
        exchanges.get(exchange)
          .getAccountService()
          .getAccountInfo()
          .getWallet()
          .getBalances()
          .entrySet()
      )
      .transform(Map.Entry::getValue)
      .filter(balance -> currencies.contains(balance.getCurrency().getCurrencyCode()))
      .transform(balance -> {
        Balance result = new Balance();
        result.currency = balance.getCurrency().getCurrencyCode();
        result.total = balance.getTotal();
        result.available = balance.getAvailable();
        return result;
      });

    return Maps.uniqueIndex(balances, balance -> balance.currency);
  }

  public static final class Balance {
    @JsonIgnore public String currency;
    @JsonProperty public BigDecimal total;
    @JsonProperty public BigDecimal available;
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
  @RolesAllowed(Roles.TRADER)
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