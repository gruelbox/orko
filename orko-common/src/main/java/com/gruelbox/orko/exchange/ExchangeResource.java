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

package com.gruelbox.orko.exchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.kucoin.service.KucoinCancelOrderParams;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.gruelbox.orko.auth.Roles;
import com.gruelbox.orko.marketdata.Balance;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

/**
 * Access to exchange information.
 */
@Path("/exchanges")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ExchangeResource implements WebResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeResource.class);

  private final ExchangeService exchanges;
  private final TradeServiceFactory tradeServiceFactory;
  private final AccountServiceFactory accountServiceFactory;

  @Inject
  ExchangeResource(ExchangeService exchanges, TradeServiceFactory tradeServiceFactory, AccountServiceFactory accountServiceFactory) {
    this.exchanges = exchanges;
    this.tradeServiceFactory = tradeServiceFactory;
    this.accountServiceFactory = accountServiceFactory;
  }


  /**
   * Identifies the supported exchanges.
   *
   * @return List of exchanges.
   */
  @GET
  @Timed
  @RolesAllowed(Roles.TRADER)
  public Collection<String> list() {
    return exchanges.getExchanges();
  }


  /**
   * Lists all currency pairs on the specified exchange.
   *
   * @param exchangeName The exchange.
   * @return The supported currency pairs.
   */
  @GET
  @Timed
  @Path("{exchange}/pairs")
  @RolesAllowed(Roles.TRADER)
  public Collection<Pair> pairs(@PathParam("exchange") String exchangeName) {

    // TODO Pending answer on https://github.com/knowm/XChange/issues/2886
    if (Exchanges.BITMEX.equals(exchangeName)) {
      return ImmutableList.of(
        new Pair("XBT", "USD"),
        new Pair("XBT", "Z18"),
        new Pair("ADA", "Z18"),
        new Pair("BCH", "Z18"),
        new Pair("EOS", "Z18"),
        new Pair("ETH", "USD"),
        new Pair("ETH", "Z18"),
        new Pair("LTC", "Z18"),
        new Pair("TRX", "Z18"),
        new Pair("XRP", "Z18")
      );
    } else {
      return exchanges.get(exchangeName)
          .getExchangeMetaData()
          .getCurrencyPairs()
          .keySet()
          .stream()
          .map(Pair::new)
          .collect(Collectors.toSet());
    }
  }

  public static class Pair {

    @JsonProperty public String counter;
    @JsonProperty public String base;

    public Pair(String base, String counter) {
      this.base = base;
      this.counter = counter;
    }

    public Pair(CurrencyPair currencyPair) {
      this.counter = currencyPair.counter.getCurrencyCode();
      this.base = currencyPair.base.getCurrencyCode();
    }
  }

  @GET
  @Timed
  @Path("{exchange}/pairs/{base}-{counter}")
  @RolesAllowed(Roles.TRADER)
  public PairMetaData metadata(@PathParam("exchange") String exchangeName, @PathParam("counter") String counter, @PathParam("base") String base) throws IOException {

    Exchange exchange = exchanges.get(exchangeName);

    // TODO Pending answer on https://github.com/knowm/XChange/issues/2886
    CurrencyPair currencyPair = new CurrencyPair(
      base.equals("Z18") ? "BTC" : base,
      counter.equals("Z18") ? "BTC" : counter
    );
    return new PairMetaData(exchange.getExchangeMetaData().getCurrencyPairs().get(currencyPair));
  }

  public static class PairMetaData {

    @JsonProperty public BigDecimal maximumAmount;
    @JsonProperty public BigDecimal minimumAmount;
    @JsonProperty public Integer priceScale;

    public PairMetaData(CurrencyPairMetaData currencyPairMetaData) {
      this.minimumAmount = currencyPairMetaData.getMinimumAmount();
      this.maximumAmount = currencyPairMetaData.getMaximumAmount();
      this.priceScale = currencyPairMetaData.getPriceScale();
    }
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
  public Response orders(@PathParam("exchange") String exchange) throws IOException {
    try {
      return Response.ok()
          .entity(tradeServiceFactory.getForExchange(exchange).getOpenOrders())
          .build();
    } catch (NotAvailableFromExchangeException e) {
      return Response.status(503).build();
    }
  }


  /**
   * Submits a new order.
   *
   * @param exchange The exchange to submit to.
   * @return
   * @throws IOException
   */
  @POST
  @Path("{exchange}/orders")
  @Timed
  @RolesAllowed(Roles.TRADER)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postOrder(@PathParam("exchange") String exchange, Map<String, String> order) throws IOException {
    if (order.containsKey("stopPrice") || !order.containsKey("limitPrice"))
      return Response.status(400).entity(ImmutableMap.of("message", "Only limit orders supported at the moment")).build();

    TradeService tradeService = tradeServiceFactory.getForExchange(exchange);

    try {
      String id = tradeService.placeLimitOrder(
        new LimitOrder(
          OrderType.valueOf(order.get("type")),
          new BigDecimal(order.get("amount")),
          new CurrencyPair(order.get("base"), order.get("counter")),
          null,
          new Date(),
          new BigDecimal(order.get("limitPrice"))
        ));
      return Response.ok()
          .entity(ImmutableMap.of("id", id))
          .build();
    } catch (NotAvailableFromExchangeException e) {
      return Response.status(503).build();
    } catch (Exception e) {
      LOGGER.error("Failed to submit order", e);
      return Response.status(500).entity(ImmutableMap.of("message", "Failed to submit order. " + e.getMessage())).build();
    }
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
  public Response orders(@PathParam("exchange") String exchangeCode,
                         @PathParam("currency") String currency) throws IOException {

    try {

      LOGGER.info("Thorough orders search...");
      Exchange exchange = exchanges.get(exchangeCode);
      return Response.ok()
        .entity(exchange
          .getExchangeMetaData()
          .getCurrencyPairs()
          .keySet()
          .stream()
          .filter(p -> p.base.getCurrencyCode().equals(currency) || p.counter.getCurrencyCode().equals(currency))
          .peek(p -> LOGGER.info("Checking " + p))
          .flatMap(p -> {
            try {
              Thread.sleep(200);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              throw new RuntimeException(e);
            }
            try {
              return exchange
                .getTradeService()
                .getOpenOrders(new DefaultOpenOrdersParamCurrencyPair(p))
                .getOpenOrders()
                .stream();
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          })
          .collect(Collectors.toList())
        ).build();
    } catch (NotAvailableFromExchangeException e) {
      return Response.status(503).build();
    }

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
  public Response orders(@PathParam("exchange") String exchange,
                           @PathParam("counter") String counter,
                           @PathParam("base") String base) throws IOException {
    try {

      CurrencyPair currencyPair = new CurrencyPair(base, counter);

      OpenOrders unfiltered = tradeServiceFactory.getForExchange(exchange)
          .getOpenOrders(new DefaultOpenOrdersParamCurrencyPair(currencyPair));

      OpenOrders filtered = new OpenOrders(
        unfiltered.getOpenOrders().stream().filter(o -> o.getCurrencyPair().equals(currencyPair)).collect(Collectors.toList()),
        unfiltered.getHiddenOrders().stream().filter(o -> o.getCurrencyPair().equals(currencyPair)).collect(Collectors.toList())
      );

      return Response.ok().entity(filtered).build();

    } catch (NotAvailableFromExchangeException e) {
      return Response.status(503).build();
    }
  }


  /**
   * Cancels an order for a specific currency pair.
   *
   * @param exchange The exchange.
   * @param counter The countercurrency.
   * @param base The base (traded) currency.
   * @param id The order id.
   * @param orderType The order type, sadly required by KuCoin.
   * @throws IOException If thrown by exchange.
   */
  @DELETE
  @Path("{exchange}/markets/{base}-{counter}/orders/{id}")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public Response cancelOrder(@PathParam("exchange") String exchange,
                              @PathParam("counter") String counter,
                              @PathParam("base") String base,
                              @PathParam("id") String id,
                              @QueryParam("orderType") org.knowm.xchange.dto.Order.OrderType orderType) throws IOException {
    try {
      // KucoinCancelOrderParams is the superset - pair, id and order type. Should work with pretty much any exchange.
      return Response.ok()
        .entity(
          tradeServiceFactory.getForExchange(exchange)
            .cancelOrder(new KucoinCancelOrderParams(new CurrencyPair(base, counter), id, orderType))
        )
        .build();
    } catch (NotAvailableFromExchangeException e) {
      return Response.status(503).build();
    }
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
  public Response order(@PathParam("exchange") String exchange, @PathParam("id") String id) throws IOException {
    try {
      return Response.ok()
          .entity(tradeServiceFactory.getForExchange(exchange).getOrder(id))
          .build();
    } catch (NotAvailableFromExchangeException e) {
      return Response.status(503).build();
    }
  }


  /**
   * Cancels the specified order. Often not supported.
   * See {@link ExchangeResource#cancelOrder(String, String, String, String)}.
   *
   * @param exchange The exchange.
   * @param id The oirder id.
   * @return The matching orders.
   * @throws IOException If thrown by exchange.
   */
  @DELETE
  @Path("{exchange}/orders/{id}")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public Response cancelOrder(@PathParam("exchange") String exchange, @PathParam("id") String id) throws IOException {
    try {
      return Response.ok()
        .entity(tradeServiceFactory.getForExchange(exchange).cancelOrder(id))
        .build();
    } catch (NotAvailableFromExchangeException e) {
      return Response.status(503).build();
    }
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
  public Response balances(@PathParam("exchange") String exchange, @PathParam("currencies") String currenciesAsString) throws IOException {

    Set<String> currencies = Stream.of(currenciesAsString.split(","))
        .collect(Collectors.toSet());

    try {

      FluentIterable<Balance> balances = FluentIterable.from(
          accountServiceFactory.getForExchange(exchange)
            .getAccountInfo()
            .getWallet()
            .getBalances()
            .entrySet()
        )
        .transform(Map.Entry::getValue)
        .filter(balance -> currencies.contains(balance.getCurrency().getCurrencyCode()))
        .transform(Balance::create);

      return Response.ok()
          .entity(Maps.uniqueIndex(balances, balance -> balance.currency()))
          .build();

    } catch (NotAvailableFromExchangeException e) {
      return Response.status(503).build();
    }
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
