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

package com.gruelbox.orko.marketdata;

import static java.util.Collections.emptySet;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.knowm.xchange.dto.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.marketdata.MarketDataSubscriptionManager.LifecycleListener;
import com.gruelbox.orko.spi.TickerSpec;

import io.reactivex.Flowable;

abstract class AbstractMarketSourceManager extends AbstractExecutionThreadService implements MarketDataSource {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private final OrkoConfiguration configuration;
  protected final CachingPersistentPublisher<TickerEvent, TickerSpec> tickersOut;
  protected final CachingPersistentPublisher<OpenOrdersEvent, TickerSpec> openOrdersOut;
  protected final CachingPersistentPublisher<OrderBookEvent, TickerSpec> orderbookOut;
  protected final PersistentPublisher<TradeEvent> tradesOut;
  protected final CachingPersistentPublisher<BalanceEvent, String> balanceOut;
  protected final PersistentPublisher<OrderChangeEvent> orderStatusChangeOut;
  protected final CachingPersistentPublisher<UserTradeEvent, String> userTradesOut;

  private final Phaser phaser = new Phaser(1);

  private LifecycleListener lifecycleListener = new LifecycleListener() {};

  protected AbstractMarketSourceManager(OrkoConfiguration configuration) {
    this.configuration = configuration;
    this.tickersOut = new CachingPersistentPublisher<>(TickerEvent::spec);
    this.openOrdersOut = new CachingPersistentPublisher<>(OpenOrdersEvent::spec);
    this.orderbookOut = new CachingPersistentPublisher<>(OrderBookEvent::spec);
    this.tradesOut = new PersistentPublisher<>();
    this.userTradesOut = new CachingPersistentPublisher<>((UserTradeEvent e) -> e.trade().getId())
        .orderInitialSnapshotBy(iterable -> Ordering.natural().onResultOf((UserTradeEvent e) -> e.trade().getTimestamp()).sortedCopy(iterable));
    this.balanceOut = new CachingPersistentPublisher<>((BalanceEvent e) -> e.exchange() + "/" + e.currency());
    this.orderStatusChangeOut = new PersistentPublisher<>();
  }


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
  @Override
  public abstract void updateSubscriptions(Set<MarketDataSubscription> subscriptions);


  /**
   * Gets the stream of subscribed tickers, starting with any cached tickers.
   *
   * @return The stream.
   */
  @Override
  public Flowable<TickerEvent> getTickers() {
    return tickersOut.getAll();
  }


  /**
   * Gets the stream of subscribed open order lists.
   *
   * @return The stream.
   */
  @Override
  public Flowable<OpenOrdersEvent> getOrderSnapshots() {
    return openOrdersOut.getAll();
  }


  /**
   * Gets a stream containing updates to the order book.
   *
   * @return The stream.
   */
  @Override
  public Flowable<OrderBookEvent> getOrderBookSnapshots() {
    return orderbookOut.getAll();
  }


  /**
   * Gets a stream of trades.
   *
   * @return The stream.
   */
  @Override
  public Flowable<TradeEvent> getTrades() {
    return tradesOut.getAll();
  }


  /**
   * Gets a stream of user trades.
   *
   * @return The stream.
   */
  @Override
  public Flowable<UserTradeEvent> getUserTrades() {
    return userTradesOut.getAll();
  }


  /**
   * Gets a stream with updates to the balance.
   *
   * @return The stream.
   */
  @Override
  public Flowable<BalanceEvent> getBalances() {
    return balanceOut.getAll();
  }


  /**
   * Call immediately after submitting an order to ensure the full order details appear
   * in the event stream at some point (allows for Coinbase not providing everything).
   *
   * TODO temporary until better support is arrange in xchange-stream
   */
  @Override
  public void postOrder(TickerSpec spec, Order order) {
    orderStatusChangeOut.emit(OrderChangeEvent.create(spec, order, new Date()));
  }


  /**
   * Gets a stream with binance execution reports.
   *
   * @return The stream.
   */
  @Override
  public Flowable<OrderChangeEvent> getOrderChanges() {
    return orderStatusChangeOut.getAll();
  }


  @Override
  protected final void run() {
    Thread.currentThread().setName(MarketDataSubscriptionManager.class.getSimpleName());
    logger.info("{} started", this);
    try {
      doRun();
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
    } catch (InterruptedException e) {
      logger.error("{} stopping due to interrupt", this, e);
    } catch (Exception e) {
      logger.error("{} stopping due to uncaught exception", this, e);
    } finally {
      updateSubscriptions(emptySet());
      logger.info("{} stopped", this);
      lifecycleListener.onStopMain();
    }
  }

  protected abstract void doRun() throws InterruptedException;


  protected void wake() {
    int phase = phaser.arrive();
    logger.debug("Progressing to phase {}", phase);
  }

  protected boolean isTerminated() {
    return phaser.isTerminated();
  }

  protected int getPhase() {
    return phaser.getPhase();
  }

  protected void subtaskStopped(String subTaskName) {
    lifecycleListener.onStop(subTaskName);
  }

  @Override
  protected void triggerShutdown() {
    super.triggerShutdown();
    phaser.arriveAndDeregister();
    phaser.forceTermination();
  }

  protected void suspend(String subTaskName, int phase, boolean failed) throws InterruptedException {
    logger.debug("{} - poll going to sleep", subTaskName);
    try {
      if (failed) {
        long defaultSleep = (long) configuration.getLoopSeconds() * 1000;
        phaser.awaitAdvanceInterruptibly(phase, defaultSleep, TimeUnit.MILLISECONDS);
      } else {
        logger.debug("{} - sleeping until phase {}", subTaskName, phase);
        lifecycleListener.onBlocked(subTaskName);
        phaser.awaitAdvanceInterruptibly(phase);
        logger.debug("{} - poll woken up on request", subTaskName);
      }
    } catch (TimeoutException e) {
      // fine
    } catch (InterruptedException e) {
      throw e;
    } catch (Exception e) {
      logger.error("Failure in phaser wait for " + subTaskName, e);
    }
  }

  @VisibleForTesting
  void setLifecycleListener(LifecycleListener listener) {
    this.lifecycleListener = listener;
  }
}
