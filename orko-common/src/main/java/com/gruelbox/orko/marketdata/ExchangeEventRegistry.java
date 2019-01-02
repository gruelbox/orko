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

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.reactivex.Flowable;

public interface ExchangeEventRegistry {

  public ExchangeEventSubscription subscribe(Set<MarketDataSubscription> targetSubscriptions);

  public default ExchangeEventSubscription subscribe(MarketDataSubscription... targetSubscriptions) {
    return subscribe(ImmutableSet.copyOf(targetSubscriptions));
  }

  public interface ExchangeEventSubscription extends AutoCloseable {
    Flowable<TickerEvent> getTickers();
    Flowable<OpenOrdersEvent> getOpenOrders();
    Flowable<OrderBookEvent> getOrderBooks();
    Flowable<TradeEvent> getTrades();
    Flowable<TradeHistoryEvent> getUserTradeHistory();
    Flowable<BalanceEvent> getBalance();

    Iterable<Flowable<TickerEvent>> getTickersSplit();
    Iterable<Flowable<TradeHistoryEvent>> getUserTradeHistorySplit();

    public ExchangeEventSubscription replace(Set<MarketDataSubscription> targetSubscriptions);

    public default ExchangeEventSubscription replace(MarketDataSubscription... targetSubscriptions) {
      return replace(ImmutableSet.copyOf(targetSubscriptions));
    }

    @Override
    void close();
  }
}