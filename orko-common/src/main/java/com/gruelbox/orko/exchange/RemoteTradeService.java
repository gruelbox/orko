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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.knowm.xchange.binance.service.BinanceCancelOrderParams;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderParams;

/**
 * Trade service implementation which simply forwards the requests to a remote market data
 * application.
 */
class RemoteTradeService implements TradeService {

  private final RemoteMarketDataConfiguration configuration;
  private final Client client;
  private final String exchange;

  @AssistedInject
  RemoteTradeService(
      RemoteMarketDataConfiguration configuration, Client client, @Assisted String exchange) {
    this.configuration = configuration;
    this.client = client;
    this.exchange = exchange;
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {
    ExchangeResource.OrderPrototype order = new ExchangeResource.OrderPrototype();
    order.setAmount(marketOrder.getOriginalAmount());
    order.setBase(marketOrder.getCurrencyPair().base.getCurrencyCode());
    order.setCounter(marketOrder.getCurrencyPair().counter.getCurrencyCode());
    order.setType(marketOrder.getType());
    return placeOrder(order);
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {
    ExchangeResource.OrderPrototype order = new ExchangeResource.OrderPrototype();
    order.setAmount(limitOrder.getOriginalAmount());
    order.setBase(limitOrder.getCurrencyPair().base.getCurrencyCode());
    order.setCounter(limitOrder.getCurrencyPair().counter.getCurrencyCode());
    order.setType(limitOrder.getType());
    order.setLimitPrice(limitOrder.getLimitPrice());
    return placeOrder(order);
  }

  @Override
  public String placeStopOrder(StopOrder stopOrder) throws IOException {
    ExchangeResource.OrderPrototype order = new ExchangeResource.OrderPrototype();
    order.setAmount(stopOrder.getOriginalAmount());
    order.setBase(stopOrder.getCurrencyPair().base.getCurrencyCode());
    order.setCounter(stopOrder.getCurrencyPair().counter.getCurrencyCode());
    order.setType(stopOrder.getType());
    order.setStopPrice(stopOrder.getStopPrice());
    order.setLimitPrice(stopOrder.getLimitPrice());
    return placeOrder(order);
  }

  private String placeOrder(ExchangeResource.OrderPrototype order) {
    Response response =
        client
            .target(String.format("%s/%s/orders", configuration.getExchangeEndpointUri(), exchange))
            .request()
            .post(Entity.json(order));
    if (response.getStatus() == 200) {
      Order orderResponse = response.readEntity(Order.class);
      return orderResponse.getId();
    } else {
      ExchangeResource.ErrorResponse errorResponse =
          response.readEntity(ExchangeResource.ErrorResponse.class);
      throw new ExchangeException(errorResponse.getMessage());
    }
  }

  @Override
  public boolean cancelOrder(CancelOrderParams orderParams) throws IOException {
    BinanceCancelOrderParams params = (BinanceCancelOrderParams) orderParams;
    Response response =
        client
            .target(
                String.format(
                    "%s/%s/markets/%s-%s/orders/%s",
                    configuration.getExchangeEndpointUri(),
                    exchange,
                    params.getCurrencyPair().base.getCurrencyCode(),
                    params.getCurrencyPair().counter.getCurrencyCode(),
                    params.getOrderId()))
            .request()
            .delete();
    return response.getStatus() == 200;
  }

  public static final class Factory {

    private final RemoteMarketDataConfiguration configuration;
    private final Client client;

    @Inject
    Factory(RemoteMarketDataConfiguration configuration, Client client) {
      this.configuration = configuration;
      this.client = client;
    }

    public RemoteTradeService create(String exchange) {
      return new RemoteTradeService(configuration, client, exchange);
    }
  }
}
