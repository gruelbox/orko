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

import com.google.common.collect.ImmutableSet;
import io.reactivex.Flowable;
import java.util.Set;

public interface ExchangeEventRegistry {

  public ExchangeEventSubscription subscribe(Set<MarketDataSubscription> targetSubscriptions);

  public default ExchangeEventSubscription subscribe(
      MarketDataSubscription... targetSubscriptions) {
    return subscribe(ImmutableSet.copyOf(targetSubscriptions));
  }

  public interface ExchangeEventSubscription extends AutoCloseable {
    Flowable<TickerEvent> getTickers();

    Flowable<OpenOrdersEvent> getOrderSnapshots();

    Flowable<OrderBookEvent> getOrderBooks();

    Flowable<TradeEvent> getTrades();

    Flowable<OrderChangeEvent> getOrderChanges();

    Flowable<UserTradeEvent> getUserTrades();

    Flowable<BalanceEvent> getBalances();

    Iterable<Flowable<TickerEvent>> getTickersSplit();

    public ExchangeEventSubscription replace(Set<MarketDataSubscription> targetSubscriptions);

    public default ExchangeEventSubscription replace(
        MarketDataSubscription... targetSubscriptions) {
      return replace(ImmutableSet.copyOf(targetSubscriptions));
    }

    @Override
    void close();
  }
}
