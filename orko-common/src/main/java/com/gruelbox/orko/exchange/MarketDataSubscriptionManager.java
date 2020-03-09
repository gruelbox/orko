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

import com.gruelbox.orko.spi.TickerSpec;
import io.reactivex.Flowable;
import org.knowm.xchange.dto.Order;

/**
 * Allows an application to subscribe to market data from any exchange. Note that this should
 * normally be used via {@link ExchangeEventRegistry}; the subscriptions managed here are
 * process-global and include the aggregated total of all subscriptions made to {@code
 * ExchangeEventRegistry}.
 */
public interface MarketDataSubscriptionManager extends SubscriptionController {

  /**
   * Gets the stream of subscribed tickers, starting with any cached tickers.
   *
   * @return The stream.
   */
  Flowable<TickerEvent> getTickers();

  /**
   * Gets the stream of subscribed open order lists.
   *
   * @return The stream.
   */
  Flowable<OpenOrdersEvent> getOrderSnapshots();

  /**
   * Gets a stream containing updates to the order book.
   *
   * @return The stream.
   */
  Flowable<OrderBookEvent> getOrderBookSnapshots();

  /**
   * Gets a stream of trades.
   *
   * @return The stream.
   */
  Flowable<TradeEvent> getTrades();

  /**
   * Gets a stream of user trades.
   *
   * @return The stream.
   */
  Flowable<UserTradeEvent> getUserTrades();

  /**
   * Gets a stream with updates to the balance.
   *
   * @return The stream.
   */
  Flowable<BalanceEvent> getBalances();

  /**
   * Gets a stream with binance execution reports.
   *
   * @return The stream.
   */
  Flowable<OrderChangeEvent> getOrderChanges();

  /**
   * Call immediately after submitting an order to ensure the full order details appear in the event
   * stream at some point (allows for Coinbase not providing everything).
   *
   * <p>TODO temporary until better support is arrange in xchange-stream
   */
  void postOrder(TickerSpec spec, Order order);
}
