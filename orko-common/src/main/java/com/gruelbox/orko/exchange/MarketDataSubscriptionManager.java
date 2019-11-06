package com.gruelbox.orko.exchange;

import org.knowm.xchange.dto.Order;

import com.gruelbox.orko.spi.TickerSpec;

import io.reactivex.Flowable;

/**
 * Allows an application to subscribe to market data from any exchange. Note that
 * this should normally be used via {@link ExchangeEventRegistry}; the
 * subscriptions managed here are process-global and include the aggregated
 * total of all subscriptions made to {@code ExchangeEventRegistry}.
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
   * Call immediately after submitting an order to ensure the full order details appear
   * in the event stream at some point (allows for Coinbase not providing everything).
   *
   * TODO temporary until better support is arrange in xchange-stream
   */
  void postOrder(TickerSpec spec, Order order);

}
