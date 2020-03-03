/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.exchange;

import static java.util.stream.Collectors.toList;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Ordering;
import com.gruelbox.orko.exchange.MaxTradeAmountCalculator.Factory;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.binance.service.BinanceCancelOrderParams;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderStatus;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.exceptions.FundsExceededException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.DefaultCancelOrderParamId;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Access to exchange information. */
@Path("/exchanges")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ExchangeResource implements WebResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeResource.class);

  private final ExchangeService exchanges;
  private final TradeServiceFactory tradeServiceFactory;
  private final AccountServiceFactory accountServiceFactory;
  private final Map<String, ExchangeConfiguration> configuration;
  private final MarketDataSubscriptionManager subscriptionManager;
  private final Factory calculatorFactory;

  @Inject
  ExchangeResource(
      ExchangeService exchanges,
      TradeServiceFactory tradeServiceFactory,
      AccountServiceFactory accountServiceFactory,
      MarketDataSubscriptionManager subscriptionManager,
      Map<String, ExchangeConfiguration> configuration,
      MaxTradeAmountCalculator.Factory calculatorFactory) {
    this.exchanges = exchanges;
    this.tradeServiceFactory = tradeServiceFactory;
    this.accountServiceFactory = accountServiceFactory;
    this.subscriptionManager = subscriptionManager;
    this.configuration = configuration;
    this.calculatorFactory = calculatorFactory;
  }

  /**
   * Identifies the supported exchanges.
   *
   * @return List of exchanges.
   */
  @GET
  @Timed
  public Collection<ExchangeMeta> list() {
    return exchanges.getExchanges().stream()
        .map(
            code -> {
              ExchangeConfiguration exchangeConfig = configuration.get(code);
              return new ExchangeMeta(
                  code,
                  Exchanges.name(code),
                  Exchanges.refLink(code),
                  exchangeConfig == null
                      ? false
                      : StringUtils.isNotBlank(exchangeConfig.getApiKey()));
            })
        .sorted(Ordering.natural().onResultOf(ExchangeMeta::getName))
        .collect(toList());
  }

  public static final class ExchangeMeta {
    @JsonProperty private final String code;
    @JsonProperty private final String name;
    @JsonProperty private final String refLink;
    @JsonProperty private final boolean authenticated;

    private ExchangeMeta(String code, String name, String refLink, boolean authenticated) {
      super();
      this.code = code;
      this.name = name;
      this.refLink = refLink;
      this.authenticated = authenticated;
    }

    String getCode() {
      return code;
    }

    String getName() {
      return name;
    }

    String getRefLink() {
      return refLink;
    }

    boolean isAuthenticated() {
      return authenticated;
    }
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
  public Collection<Pair> pairs(@PathParam("exchange") String exchangeName) {
    return exchanges.get(exchangeName).getExchangeMetaData().getCurrencyPairs().keySet().stream()
        .map(Pair::new)
        .collect(Collectors.toSet());
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
  public PairMetaData metadata(
      @PathParam("exchange") String exchangeName,
      @PathParam("counter") String counter,
      @PathParam("base") String base) {
    Exchange exchange = exchanges.get(exchangeName);
    CurrencyPair currencyPair = new CurrencyPair(base, counter);
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
   * Fetches all open orders on the specified exchange. Often not supported. See {@link
   * ExchangeResource#orders(String, String)}.
   *
   * @param exchange The exchange.
   * @return The open orders.
   * @throws IOException If thrown by exchange.
   */
  @GET
  @Path("{exchange}/orders")
  @Timed
  public Response orders(@PathParam("exchange") String exchange) throws IOException {
    try {
      return Response.ok()
          .entity(tradeServiceFactory.getForExchange(exchange).getOpenOrders())
          .build();
    } catch (NotAvailableFromExchangeException e) {
      return Response.status(503).build();
    }
  }

  @POST
  @Path("{exchange}/orders/calc")
  @Timed
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response calculateOrder(@PathParam("exchange") String exchange, OrderPrototype order) {
    if (!order.isLimit()) {
      return Response.status(400).entity(new ErrorResponse("Limit price required")).build();
    }
    TickerSpec tickerSpec =
        TickerSpec.builder()
            .exchange(exchange)
            .base(order.getBase())
            .counter(order.getCounter())
            .build();
    BigDecimal orderAmount =
        calculatorFactory
            .create(tickerSpec)
            .validOrderAmount(order.getLimitPrice(), order.getType());
    order.setAmount(orderAmount);
    return Response.ok().entity(order).build();
  }

  /**
   * Submits a new order.
   *
   * @param exchange The exchange to submit to.
   * @return HTTP response.
   */
  @POST
  @Path("{exchange}/orders")
  @Timed
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postOrder(@PathParam("exchange") String exchange, OrderPrototype order) {
    Optional<Response> error = checkOrderPreconditions(exchange, order);
    if (error.isPresent()) return error.get();

    try {
      TradeService tradeService = tradeServiceFactory.getForExchange(exchange);
      Order result =
          order.isStop() ? postStopOrder(order, tradeService) : postLimitOrder(order, tradeService);
      postOrderToSubscribers(exchange, result);
      return Response.ok().entity(result).build();
    } catch (NotAvailableFromExchangeException e) {
      return Response.status(503)
          .entity(new ErrorResponse("Order type not currently supported by exchange."))
          .build();
    } catch (FundsExceededException e) {
      return Response.status(400).entity(new ErrorResponse(e.getMessage())).build();
    } catch (Exception e) {
      LOGGER.error("Failed to submit order: {}", order, e);
      return Response.status(500)
          .entity(new ErrorResponse("Failed to submit order. " + e.getMessage()))
          .build();
    }
  }

  private Optional<Response> checkOrderPreconditions(String exchange, OrderPrototype order) {
    if (!order.isStop() && !order.isLimit())
      return Optional.of(
          Response.status(400)
              .entity(new ErrorResponse("Market orders not supported at the moment."))
              .build());

    if (order.isStop()) {
      if (order.isLimit()) {
        if (exchange.equals(Exchanges.BITFINEX)) {
          return Optional.of(
              Response.status(400)
                  .entity(
                      new ErrorResponse(
                          "Stop limit orders not supported for Bitfinex at the moment."))
                  .build());
        }
      } else {
        if (exchange.equals(Exchanges.BINANCE)) {
          return Optional.of(
              Response.status(400)
                  .entity(
                      new ErrorResponse(
                          "Stop market orders not supported for Binance at the moment. Specify a limit price."))
                  .build());
        }
      }
    }

    return Optional.empty();
  }

  private LimitOrder postLimitOrder(OrderPrototype order, TradeService tradeService)
      throws IOException {
    LimitOrder limitOrder =
        new LimitOrder(
            order.getType(),
            order.getAmount(),
            new CurrencyPair(order.getBase(), order.getCounter()),
            null,
            new Date(),
            order.getLimitPrice());
    String id = tradeService.placeLimitOrder(limitOrder);
    return LimitOrder.Builder.from(limitOrder).id(id).orderStatus(OrderStatus.NEW).build();
  }

  private StopOrder postStopOrder(OrderPrototype order, TradeService tradeService)
      throws IOException {
    StopOrder stopOrder =
        new StopOrder(
            order.getType(),
            order.getAmount(),
            new CurrencyPair(order.getBase(), order.getCounter()),
            null,
            new Date(),
            order.getStopPrice(),
            order.getLimitPrice(),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            OrderStatus.PENDING_NEW);
    String id = tradeService.placeStopOrder(stopOrder);
    return StopOrder.Builder.from(stopOrder).id(id).orderStatus(OrderStatus.NEW).build();
  }

  private void postOrderToSubscribers(String exchange, Order order) {
    CurrencyPair currencyPair = order.getCurrencyPair();
    subscriptionManager.postOrder(
        TickerSpec.builder()
            .exchange(exchange)
            .base(currencyPair.base.getCurrencyCode())
            .counter(currencyPair.counter.getCurrencyCode())
            .build(),
        order);
  }

  /**
   * Fetches all open orders the the specified currency, on all pairs for that currency. May take
   * some time; lots of consecutive API calls are required for each pair.
   *
   * @param exchangeCode The exchange.
   * @param currency The currency.
   * @return The open orders.
   * @throws IOException If thrown by exchange.
   */
  @GET
  @Path("{exchange}/currencies/{currency}/orders")
  @Timed
  public Response orders(
      @PathParam("exchange") String exchangeCode, @PathParam("currency") String currency)
      throws IOException {

    try {

      LOGGER.info("Thorough orders search...");
      Exchange exchange = exchanges.get(exchangeCode);
      return Response.ok()
          .entity(
              exchange.getExchangeMetaData().getCurrencyPairs().keySet().stream()
                  .filter(
                      p ->
                          p.base.getCurrencyCode().equals(currency)
                              || p.counter.getCurrencyCode().equals(currency))
                  .flatMap(
                      p -> {
                        try {
                          Thread.sleep(200);
                        } catch (InterruptedException e) {
                          Thread.currentThread().interrupt();
                          throw new RuntimeException(e);
                        }
                        try {
                          return exchange.getTradeService()
                              .getOpenOrders(new DefaultOpenOrdersParamCurrencyPair(p))
                              .getOpenOrders().stream();
                        } catch (IOException e) {
                          throw new RuntimeException(e);
                        }
                      })
                  .collect(Collectors.toList()))
          .build();
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
  public Response orders(
      @PathParam("exchange") String exchange,
      @PathParam("counter") String counter,
      @PathParam("base") String base)
      throws IOException {
    try {

      CurrencyPair currencyPair = new CurrencyPair(base, counter);

      OpenOrders unfiltered =
          tradeServiceFactory
              .getForExchange(exchange)
              .getOpenOrders(new DefaultOpenOrdersParamCurrencyPair(currencyPair));

      OpenOrders filtered =
          new OpenOrders(
              unfiltered.getOpenOrders().stream()
                  .filter(o -> o.getCurrencyPair().equals(currencyPair))
                  .collect(Collectors.toList()),
              unfiltered.getHiddenOrders().stream()
                  .filter(o -> o.getCurrencyPair().equals(currencyPair))
                  .collect(Collectors.toList()));

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
   * @throws IOException If thrown by exchange.
   */
  @DELETE
  @Path("{exchange}/markets/{base}-{counter}/orders/{id}")
  @Timed
  public Response cancelOrder(
      @PathParam("exchange") String exchange,
      @PathParam("counter") String counter,
      @PathParam("base") String base,
      @PathParam("id") String id)
      throws IOException {
    try {
      // BinanceCancelOrderParams is the superset - pair and id. Should work with pretty much any
      // exchange,
      // except Bitmex
      // TODO PR to fix bitmex
      CancelOrderParams cancelOrderParams =
          exchange.equals(Exchanges.BITMEX)
              ? new DefaultCancelOrderParamId(id)
              : new BinanceCancelOrderParams(new CurrencyPair(base, counter), id);
      Date now = new Date();
      if (!tradeServiceFactory.getForExchange(exchange).cancelOrder(cancelOrderParams)) {
        throw new IllegalStateException("Order could not be cancelled");
      }
      return Response.ok().entity(now).build();
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
  public Response order(@PathParam("exchange") String exchange, @PathParam("id") String id)
      throws IOException {
    try {
      return Response.ok()
          .entity(tradeServiceFactory.getForExchange(exchange).getOrder(id))
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
  public Ticker ticker(
      @PathParam("exchange") String exchange,
      @PathParam("counter") String counter,
      @PathParam("base") String base)
      throws IOException {
    return exchanges
        .get(exchange)
        .getMarketDataService()
        .getTicker(new CurrencyPair(base, counter));
  }

  public static final class OrderPrototype {

    @JsonProperty private String counter;
    @JsonProperty private String base;
    @JsonProperty @Nullable private BigDecimal stopPrice;
    @JsonProperty @Nullable private BigDecimal limitPrice;
    @JsonProperty private Order.OrderType type;
    @JsonProperty private BigDecimal amount;

    public String getCounter() {
      return counter;
    }

    public String getBase() {
      return base;
    }

    public BigDecimal getStopPrice() {
      return stopPrice;
    }

    public BigDecimal getLimitPrice() {
      return limitPrice;
    }

    public Order.OrderType getType() {
      return type;
    }

    public BigDecimal getAmount() {
      return amount;
    }

    @JsonIgnore
    boolean isStop() {
      return stopPrice != null;
    }

    @JsonIgnore
    boolean isLimit() {
      return limitPrice != null;
    }

    public void setCounter(String counter) {
      this.counter = counter;
    }

    public void setBase(String base) {
      this.base = base;
    }

    public void setStopPrice(BigDecimal stopPrice) {
      this.stopPrice = stopPrice;
    }

    public void setLimitPrice(BigDecimal limitPrice) {
      this.limitPrice = limitPrice;
    }

    public void setType(Order.OrderType type) {
      this.type = type;
    }

    public void setAmount(BigDecimal amount) {
      this.amount = amount;
    }

    @Override
    public String toString() {
      return "OrderPrototype{"
          + "counter='"
          + counter
          + '\''
          + ", base='"
          + base
          + '\''
          + ", stopPrice="
          + stopPrice
          + ", limitPrice="
          + limitPrice
          + ", type="
          + type
          + ", amount="
          + amount
          + '}';
    }
  }

  public static final class ErrorResponse {

    @JsonProperty private String message;

    ErrorResponse() {}

    ErrorResponse(String message) {
      this.message = message;
    }

    String getMessage() {
      return message;
    }

    void setMessage(String message) {
      this.message = message;
    }
  }
}
