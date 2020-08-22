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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Ordering;
import com.google.inject.Singleton;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.xtension.ExchangePollLoopPublisher;
import com.gruelbox.orko.xtension.ExchangePollLoopSubscription;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrade;

/** Central fanout point for market data. */
@VisibleForTesting
@Singleton
public final class SubscriptionPublisher implements MarketDataSubscriptionManager {

  private final CachingPersistentPublisher<TickerEvent, TickerSpec> tickersOut;
  private final CachingPersistentPublisher<OpenOrdersEvent, TickerSpec> openOrdersOut;
  private final CachingPersistentPublisher<OrderBookEvent, TickerSpec> orderbookOut;
  private final PersistentPublisher<TradeEvent> tradesOut;
  private final CachingPersistentPublisher<BalanceEvent, String> balanceOut;
  private final PersistentPublisher<OrderChangeEvent> orderStatusChangeOut;
  private final CachingPersistentPublisher<UserTradeEvent, String> userTradesOut;

  private SubscriptionController controller;

  @VisibleForTesting
  public SubscriptionPublisher() {
    this.tickersOut = new CachingPersistentPublisher<>(TickerEvent::spec);
    this.openOrdersOut = new CachingPersistentPublisher<>(OpenOrdersEvent::spec);
    this.orderbookOut = new CachingPersistentPublisher<>(OrderBookEvent::spec);
    this.tradesOut = new PersistentPublisher<>();
    this.userTradesOut =
        new CachingPersistentPublisher<>((UserTradeEvent e) -> e.trade().getId())
            .orderInitialSnapshotBy(
                iterable ->
                    Ordering.natural()
                        .onResultOf((UserTradeEvent e) -> e.trade().getTimestamp())
                        .sortedCopy(iterable));
    this.balanceOut =
        new CachingPersistentPublisher<>(
            (BalanceEvent e) -> e.exchange() + "/" + e.balance().getCurrency());
    this.orderStatusChangeOut = new PersistentPublisher<>();
  }

  void setController(SubscriptionController controller) {
    this.controller = controller;
  }

  @Override
  public Completable updateSubscriptions(Set<MarketDataSubscription> subscriptions) {
    if (this.controller == null) {
      return Completable.error(new IllegalStateException(
          SubscriptionPublisher.class.getSimpleName() + " not initialised"));
    }
    return this.controller.updateSubscriptions(subscriptions);
  }

  @Override
  public Flowable<TickerEvent> getTickers() {
    return tickersOut.getAll();
  }

  @Override
  public Flowable<OpenOrdersEvent> getOrderSnapshots() {
    return openOrdersOut.getAll();
  }

  @Override
  public Flowable<OrderBookEvent> getOrderBookSnapshots() {
    return orderbookOut.getAll();
  }

  @Override
  public Flowable<TradeEvent> getTrades() {
    return tradesOut.getAll();
  }

  @Override
  public Flowable<UserTradeEvent> getUserTrades() {
    return userTradesOut.getAll();
  }

  @Override
  public Flowable<BalanceEvent> getBalances() {
    return balanceOut.getAll();
  }

  @Override
  public Flowable<OrderChangeEvent> getOrderChanges() {
    return orderStatusChangeOut.getAll();
  }

  @Override
  public void postOrder(TickerSpec spec, Order order) {
    orderStatusChangeOut.emit(OrderChangeEvent.create(spec, order, new Date()));
  }

  void emit(TickerEvent e) {
    tickersOut.emit(e);
  }

  void emit(OpenOrdersEvent e) {
    openOrdersOut.emit(e);
  }

  void emit(OrderBookEvent e) {
    orderbookOut.emit(e);
  }

  void emit(TradeEvent e) {
    tradesOut.emit(e);
  }

  void emit(UserTradeEvent e) {
    userTradesOut.emit(e);
  }

  void emit(BalanceEvent e) {
    balanceOut.emit(e);
  }

  void emit(OrderChangeEvent e) {
    orderStatusChangeOut.emit(e);
  }

  void clearCacheForSubscription(MarketDataSubscription subscription) {
    tickersOut.removeFromCache(subscription.spec());
    orderbookOut.removeFromCache(subscription.spec());
    openOrdersOut.removeFromCache(subscription.spec());
    userTradesOut.removeFromCache(t -> t.spec().equals(subscription.spec()));
    balanceOut.removeFromCache(subscription.spec().exchange() + "/" + subscription.spec().base());
    balanceOut.removeFromCache(
        subscription.spec().exchange() + "/" + subscription.spec().counter());
  }

  ExchangePollLoopPublisher toPollLoopPublisher(String exchangeName) {
    return new ExchangePollLoopPublisher() {
      @Override
      public void emitTicker(Ticker ticker) {
        SubscriptionPublisher.this.emit(TickerEvent.create(toSpec(ticker.getCurrencyPair()), ticker));
      }

      @Override
      public void emitOpenOrders(Pair<CurrencyPair, OpenOrders> openOrders, Date timestamp) {
        SubscriptionPublisher.this.emit(OpenOrdersEvent.create(toSpec(openOrders.getLeft()), openOrders.getRight(), timestamp));
      }

      @Override
      public void emitOrderBook(Pair<CurrencyPair, OrderBook> currencyPairOrderBookPair) {
        SubscriptionPublisher.this.emit(OrderBookEvent.create(toSpec(currencyPairOrderBookPair.getLeft()), currencyPairOrderBookPair.getRight()));
      }

      @Override
      public void emitTrade(Trade trade) {
        SubscriptionPublisher.this.emit(TradeEvent.create(toSpec(trade.getCurrencyPair()), trade));
      }

      @Override
      public void emitUserTrade(UserTrade trade) {
        SubscriptionPublisher.this.emit(UserTradeEvent.create(toSpec(trade.getCurrencyPair()), trade));
      }

      @Override
      public void emitBalance(Balance balance, Instant instant) {
        SubscriptionPublisher.this.emit(BalanceEvent.create(exchangeName, balance));
      }

      @Override
      public void emitOrder(Order order, Instant instant) {
        // TODO need server side timestamping
        SubscriptionPublisher.this.emit(OrderChangeEvent.create(toSpec(order.getCurrencyPair()), order, Date.from(instant)));
      }

      @Override
      public void clearCacheForSubscription(ExchangePollLoopSubscription subscription) {
        SubscriptionPublisher.this.clearCacheForSubscription(toMarketDataSubscription(subscription));
      }

      @Override
      public void clearCaches() {
        // TODO
      }

      private MarketDataSubscription toMarketDataSubscription(ExchangePollLoopSubscription s) {
        return MarketDataSubscription.create(toSpec(s), s.type());
      }

      private TickerSpec toSpec(ExchangePollLoopSubscription s) {
        return toSpec(s.currencyPair());
      }

      private TickerSpec toSpec(CurrencyPair currencyPair) {
        return TickerSpec.builder()
            .exchange(exchangeName)
            .base(currencyPair.base.getCurrencyCode())
            .counter(currencyPair.counter.getCurrencyCode())
            .build();
      }
    };
  }
}
