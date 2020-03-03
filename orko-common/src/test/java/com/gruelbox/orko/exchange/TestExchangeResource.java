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

import static java.math.BigDecimal.ZERO;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.gruelbox.orko.exchange.ExchangeResource.ErrorResponse;
import com.gruelbox.orko.exchange.ExchangeResource.OrderPrototype;
import com.gruelbox.orko.spi.TickerSpec;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.FundsExceededException;
import org.knowm.xchange.service.trade.TradeService;
import org.mockito.Mockito;

public class TestExchangeResource {

  private static final String EXCHANGE = "something";

  private static ExchangeService exchangeService = mock(ExchangeService.class);
  private static TradeServiceFactory tradeServiceFactory = mock(TradeServiceFactory.class);
  private static AccountServiceFactory accountServiceFactory = mock(AccountServiceFactory.class);
  private static MarketDataSubscriptionManager subscriptionManager =
      mock(MarketDataSubscriptionManager.class);
  private static MaxTradeAmountCalculator.Factory calculatorFactory =
      mock(MaxTradeAmountCalculator.Factory.class);

  private static TradeService tradeService = mock(TradeService.class);

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder()
          .addResource(
              new ExchangeResource(
                  exchangeService,
                  tradeServiceFactory,
                  accountServiceFactory,
                  subscriptionManager,
                  Collections.emptyMap(),
                  calculatorFactory))
          .build();

  @Before
  public void setup() {
    when(tradeServiceFactory.getForExchange(EXCHANGE)).thenReturn(tradeService);
  }

  @After
  public void tearDown() {
    reset(exchangeService, tradeServiceFactory, accountServiceFactory, subscriptionManager);
  }

  @Test
  public void testCalculate() throws IOException {

    // Given
    OrderPrototype order = new ExchangeResource.OrderPrototype();
    order.setAmount(new BigDecimal(300));
    order.setLimitPrice(new BigDecimal(1000));
    order.setBase("XXX");
    order.setCounter("YYY");
    order.setType(ASK);

    MaxTradeAmountCalculator calculator = mock(MaxTradeAmountCalculator.class);
    when(calculator.validOrderAmount(new BigDecimal(1000), ASK)).thenReturn(new BigDecimal(200));
    when(calculatorFactory.create(TickerSpec.fromKey(EXCHANGE + "/YYY/XXX")))
        .thenReturn(calculator);

    // When
    Response response =
        resources
            .target("/exchanges/" + EXCHANGE + "/orders/calc")
            .request()
            .post(entity(order, APPLICATION_JSON));

    // Then
    OrderPrototype result = response.readEntity(OrderPrototype.class);
    assertThat(result.getAmount(), equalTo(new BigDecimal(200)));
  }

  @Test
  public void testCalculateNoLimitPrice() throws IOException {

    // Given
    OrderPrototype order = new ExchangeResource.OrderPrototype();
    order.setAmount(new BigDecimal(300));
    order.setBase("XXX");
    order.setCounter("YYY");
    order.setType(ASK);

    // When
    Response response =
        resources
            .target("/exchanges/" + EXCHANGE + "/orders/calc")
            .request()
            .post(entity(order, APPLICATION_JSON));

    // Then
    assertThat(response.getStatus(), equalTo(400));
    assertThat(
        response.readEntity(ErrorResponse.class).getMessage(), equalTo("Limit price required"));
  }

  @Test
  public void testMarketOrder() throws IOException {

    // When
    OrderPrototype order = new ExchangeResource.OrderPrototype();
    order.setAmount(ZERO);
    order.setBase("XXX");
    order.setCounter("YYY");
    order.setType(ASK);
    Response response = placeLimitOrder(EXCHANGE, order);

    // Then
    assertThat(response.getStatus(), equalTo(400));
    assertThat(
        response.readEntity(ErrorResponse.class).getMessage(),
        equalTo("Market orders not supported at the moment."));
  }

  @Test
  public void testStopLimitBitfinex() throws IOException {

    // When
    OrderPrototype order = new ExchangeResource.OrderPrototype();
    order.setAmount(ZERO);
    order.setBase("XXX");
    order.setCounter("YYY");
    order.setType(ASK);
    order.setStopPrice(ZERO);
    order.setLimitPrice(ZERO);
    Response response = placeLimitOrder(Exchanges.BITFINEX, order);

    // Then
    assertThat(response.getStatus(), equalTo(400));
    assertThat(
        response.readEntity(ErrorResponse.class).getMessage(),
        equalTo("Stop limit orders not supported for Bitfinex at the moment."));
  }

  @Test
  public void testStopMarketBinance() throws IOException {

    // When
    OrderPrototype order = new ExchangeResource.OrderPrototype();
    order.setAmount(ZERO);
    order.setBase("XXX");
    order.setCounter("YYY");
    order.setType(ASK);
    order.setStopPrice(ZERO);
    Response response = placeLimitOrder(Exchanges.BINANCE, order);

    // Then
    assertThat(response.getStatus(), equalTo(400));
    assertThat(
        response.readEntity(ErrorResponse.class).getMessage(),
        equalTo(
            "Stop market orders not supported for Binance at the moment. Specify a limit price."));
  }

  @Test
  public void testLimitOrderFundsExceeded() throws IOException {

    // Given
    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class)))
        .thenThrow(new FundsExceededException("Owch"));

    // When
    Response response = placeLimitOrder();

    // Then
    assertThat(response.getStatus(), equalTo(400));
    assertThat(response.readEntity(ErrorResponse.class).getMessage(), equalTo("Owch"));
  }

  @Test
  public void testLimitOrderServerError() throws IOException {

    // Given
    when(tradeService.placeLimitOrder(Mockito.any(LimitOrder.class)))
        .thenThrow(new ExchangeException("Anything could have happened"));

    // When
    Response response = placeLimitOrder();

    // Then
    assertThat(response.getStatus(), equalTo(500));
    assertThat(
        response.readEntity(ErrorResponse.class).getMessage(),
        equalTo("Failed to submit order. Anything could have happened"));
  }

  private Response placeLimitOrder() {
    OrderPrototype order = new ExchangeResource.OrderPrototype();
    order.setAmount(ZERO);
    order.setBase("XXX");
    order.setCounter("YYY");
    order.setLimitPrice(ZERO);
    order.setType(ASK);
    return placeLimitOrder(EXCHANGE, order);
  }

  private Response placeLimitOrder(String exchange, OrderPrototype order) {
    return resources
        .target("/exchanges/" + exchange + "/orders")
        .request()
        .post(entity(order, APPLICATION_JSON));
  }
}
