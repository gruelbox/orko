package com.grahamcrockford.oco.web.service;

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
import com.grahamcrockford.oco.api.auth.Roles;
import com.grahamcrockford.oco.api.exchange.ExchangeService;
import com.grahamcrockford.oco.web.WebResource;

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


  /**
   * Identifies the supported exchanges.
   *
   * @return List of exchanges.
   */
  @GET
  @Timed
  @RolesAllowed(Roles.PUBLIC)
  public Collection<String> list() {
    return exchanges.getExchanges();
  }


  /**
   * Lists all currency pairs on the specified exchange.
   *
   * @param exchange The exchange.
   * @return The supported currency pairs.
   */
  @GET
  @Timed
  @Path("{exchange}/pairs")
  @RolesAllowed(Roles.PUBLIC)
  public Collection<Pair> pairs(@PathParam("exchange") String exchange) {
    return exchanges.get(exchange)
        .getExchangeMetaData()
        .getCurrencyPairs()
        .keySet()
        .stream()
        .map(currencyPair -> {
          Pair pair = new Pair();
          pair.counter = currencyPair.counter.getCurrencyCode();
          pair.base = currencyPair.base.getCurrencyCode();
          return pair;
        })
        .collect(Collectors.toSet());
  }


  public static class Pair {
    @JsonProperty public String counter;
    @JsonProperty public String base;
  }


  /**
   * Fetches all open orders on the specified exchange. Often not supported.
   * See {@link ExchangeResource#orders(String, String)}.
   *
   * @param exchange The exchange.
   * @return The open orders.
   * @throws IOException If thrown by exchange.
   */
  @GET
  @Path("{exchange}/orders")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public OpenOrders orders(@PathParam("exchange") String exchange) throws IOException {
    return exchanges.get(exchange)
        .getTradeService()
        .getOpenOrders();
  }


  /**
   * Fetches all open orders the the specified currency, on all pairs
   * for that currency.  May take some time; lots of consecutive API
   * calls are required for each pair.
   *
   * @param exchangeCode The exchange.
   * @param currency The currency.
   * @return The open orders.
   * @throws IOException If thrown by exchange.
   */
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


  /**
   * Fetches open orders for the specific currency pair.
   *
   * @param exchange The exchange.
   * @param counter The countercurrency.
   * @param base The base (traded) currency.
   * @return The open orders.
   * @throws IOException If thrown by exchange.
   */
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


  /**
   * Fetches the specified order.
   *
   * @param exchange The exchange.
   * @param id The oirder id.
   * @return The matching orders.
   * @throws IOException If thrown by exchange.
   */
  @GET
  @Path("{exchange}/orders/{id}")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public Collection<Order> order(@PathParam("exchange") String exchange, @PathParam("id") String id) throws IOException {
    return exchanges.get(exchange)
        .getTradeService()
        .getOrder(id);
  }


  /**
   * Fetches the current balances for the specified exchange and currencies.
   *
   * @param exchange The exchange.
   * @param currenciesAsString Comma-separated list of currencies.
   * @return The balances, by currency.
   * @throws IOException If thrown by exchange.
   */
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


  /**
   * Gets the current ticker for the specified exchange and pair.
   *
   * @param exchange The exchange.
   * @param counter The countercurrency.
   * @param base The base (traded) currency.
   * @return The ticker.
   * @throws IOException If thrown by exchange.
   */
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