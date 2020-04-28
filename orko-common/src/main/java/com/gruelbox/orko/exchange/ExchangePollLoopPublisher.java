package com.gruelbox.orko.exchange;

import java.util.Date;
import org.apache.commons.lang3.tuple.Pair;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrade;

public interface ExchangePollLoopPublisher {
  void emitTicker(Ticker ticker);
  void emitOpenOrders(Pair<CurrencyPair, OpenOrders> openOrders, Date timestamp);
  void emitOrderBook(Pair<CurrencyPair, OrderBook> currencyPairOrderBookPair);
  void emitTrade(Trade e);
  void emitUserTrade(UserTrade e);
  void emitBalance(Balance e);
  void emitOrder(Order e);
  void clearCacheForSubscription(ExchangePollLoopSubscription subscription);
}
