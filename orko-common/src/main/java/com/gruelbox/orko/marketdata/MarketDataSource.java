package com.gruelbox.orko.marketdata;

import java.util.Set;

import org.knowm.xchange.dto.Order;

import com.gruelbox.orko.spi.TickerSpec;

import io.reactivex.Flowable;

public interface MarketDataSource {

  /**
   * Updates the subscriptions for the specified exchanges on the next loop
   * tick. The delay is to avoid a large number of new subscriptions in quick
   * succession causing rate bans on exchanges. Call with an empty set to cancel
   * all subscriptions. None of the streams (e.g. {@link #getTickers()}
   * will return anything until this is called, but there is no strict order in
   * which they need to be called.
   *
   * @param subscriptions The subscriptions.
   */
  void updateSubscriptions(Set<MarketDataSubscription> subscriptions);

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
   * Call immediately after submitting an order to ensure the full order details appear
   * in the event stream at some point (allows for Coinbase not providing everything).
   *
   * TODO temporary until better support is arrange in xchange-stream
   */
  void postOrder(TickerSpec spec, Order order);

  /**
   * Gets a stream with binance execution reports.
   *
   * @return The stream.
   */
  Flowable<OrderChangeEvent> getOrderChanges();

}