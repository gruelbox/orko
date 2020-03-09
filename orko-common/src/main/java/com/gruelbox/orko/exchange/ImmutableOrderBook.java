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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.Date;
import java.util.List;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;

/** An immutable version of {@link OrderBook}, for safe multi-thread use. */
public class ImmutableOrderBook {

  /** the asks */
  private final List<LimitOrder> asks;
  /** the bids */
  private final List<LimitOrder> bids;

  /** the timestamp of the orderbook according to the exchange's server, null if not provided */
  private final Date timeStamp;

  ImmutableOrderBook(OrderBook orderBook) {
    this(orderBook.getTimeStamp(), orderBook.getAsks(), orderBook.getBids());
  }

  /**
   * Constructor
   *
   * @param timeStamp - the timestamp of the orderbook according to the exchange's server, null if
   *     not provided
   * @param asks The ASK orders
   * @param bids The BID orders
   */
  @JsonCreator
  public ImmutableOrderBook(
      @JsonProperty("timeStamp") Date timeStamp,
      @JsonProperty("asks") List<LimitOrder> asks,
      @JsonProperty("bids") List<LimitOrder> bids) {
    this.timeStamp = timeStamp;
    this.asks = ImmutableList.copyOf(asks);
    this.bids = ImmutableList.copyOf(bids);
  }

  public Date getTimeStamp() {
    return timeStamp == null ? null : new Date(timeStamp.getTime());
  }

  public List<LimitOrder> getAsks() {
    return asks;
  }

  public List<LimitOrder> getBids() {
    return bids;
  }

  public List<LimitOrder> getOrders(OrderType type) {
    return type == OrderType.ASK ? asks : bids;
  }

  @Override
  public int hashCode() {

    int hash = 17;
    hash = 31 * hash + (this.timeStamp != null ? this.timeStamp.hashCode() : 0);
    for (LimitOrder order : this.bids) {
      hash = 31 * hash + order.hashCode();
    }
    for (LimitOrder order : this.asks) {
      hash = 31 * hash + order.hashCode();
    }
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ImmutableOrderBook other = (ImmutableOrderBook) obj;
    if (this.timeStamp == null
        ? other.timeStamp != null
        : !this.timeStamp.equals(other.timeStamp)) {
      return false;
    }
    return ordersEqual(other);
  }

  /**
   * Identical to {@link #equals(Object) equals} method except that this ignores different
   * timestamps. In other words, this version of equals returns true if the order internal to the
   * OrderBooks are equal but their timestamps are unequal. It returns false if false if any order
   * between the two are different.
   *
   * @param other The order book to which to compare.
   * @return True if the orders are equal.
   */
  public boolean ordersEqual(ImmutableOrderBook other) {
    if (other == null) {
      return false;
    }
    if (asks == null) {
      if (other.asks != null) {
        return false;
      }
    } else if (!asks.equals(other.asks)) {
      return false;
    }
    if (bids == null) {
      if (other.bids != null) {
        return false;
      }
    } else if (!bids.equals(other.bids)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {

    return "ImmutableOrderBook [timestamp: "
        + timeStamp
        + ", asks="
        + asks.toString()
        + ", bids="
        + bids.toString()
        + "]";
  }
}
