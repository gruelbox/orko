package com.grahamcrockford.orko.marketdata;

import java.util.Date;
import java.util.List;

import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;

import com.google.common.collect.ImmutableList;

/**
 * An immutable version of {@link OrderBook}, for safe multi-thread use.
 */
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
  ImmutableOrderBook(Date timeStamp, List<LimitOrder> asks, List<LimitOrder> bids) {
    this.timeStamp = timeStamp;
    this.asks = ImmutableList.copyOf(asks);
    this.bids = ImmutableList.copyOf(bids);
  }

  public Date getTimeStamp() {
    return timeStamp;
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
    if (this.bids.size() != other.bids.size()) {
      return false;
    }
    for (int index = 0; index < this.bids.size(); index++) {
      if (!this.bids.get(index).equals(other.bids.get(index))) {
        return false;
      }
    }
    if (this.asks.size() != other.asks.size()) {
      return false;
    }
    for (int index = 0; index < this.asks.size(); index++) {
      if (!this.asks.get(index).equals(other.asks.get(index))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Identical to {@link #equals(Object) equals} method except that this ignores different
   * timestamps. In other words, this version of equals returns true if the order internal to the
   * OrderBooks are equal but their timestamps are unequal. It returns false if false if any order
   * between the two are different.
   *
   * @param ob
   * @return
   */
  public boolean ordersEqual(ImmutableOrderBook ob) {
    Date timestamp = new Date();
    if (this != null && ob != null) {
      ImmutableOrderBook thisOb = new ImmutableOrderBook(timestamp, this.getAsks(), this.getBids());
      ImmutableOrderBook thatOb = new ImmutableOrderBook(timestamp, ob.getAsks(), ob.getBids());
      return thisOb.equals(thatOb);
    } else {
      return this.equals(ob);
    }
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