package com.grahamcrockford.oco.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.grahamcrockford.oco.api.TradeServiceFactory;

/**
 * Paper trading implementation.  Note: doesn't work between restarts. Probably not thread
 * safe either yet.
 */
final class PaperTradeService implements TradeService {

  private final AtomicLong orderCounter = new AtomicLong();
  private final ConcurrentMap<Long, LimitOrder> openOrders = new ConcurrentHashMap<>();
  private final ConcurrentMap<Long, Date> placedDates = new ConcurrentHashMap<>();

  private final MarketDataService marketDataService;
  private final Random random = new Random();
  private Date lastMarketUpdate = new Date();

  private PaperTradeService(MarketDataService marketDataService) {
    this.marketDataService = marketDataService;
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {
    updateAgainstMarket();
    return new OpenOrders(ImmutableList.copyOf(openOrders.values()));
  }

  @Override
  public OpenOrders getOpenOrders(OpenOrdersParams params) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {
    randomDelay();
    final long id = orderCounter.incrementAndGet();
    synchronized (this) {
      openOrders.put(id, limitOrder);
      placedDates.put(id, new Date());

      // TODO GDAX rejection?
      limitOrder.setOrderStatus(Order.OrderStatus.NEW);
    }
    updateAgainstMarket();
    return String.valueOf(id);
  }

  @Override
  public String placeStopOrder(StopOrder stopOrder) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized boolean cancelOrder(String orderId) throws IOException {
    updateAgainstMarket();
    final Long id = Long.valueOf(orderId);
    final LimitOrder limitOrder = openOrders.get(id);
    if (limitOrder == null) {
      throw new IllegalArgumentException("No such order: " + orderId);
    }
    if (ImmutableSet.of(Order.OrderStatus.CANCELED, Order.OrderStatus.FILLED).contains(limitOrder.getStatus())) {
      return false;
    }
    limitOrder.setOrderStatus(Order.OrderStatus.CANCELED);
    return true;
  }

  @Override
  public boolean cancelOrder(CancelOrderParams orderParams) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public TradeHistoryParams createTradeHistoryParams() {
    throw new UnsupportedOperationException();
  }

  @Override
  public OpenOrdersParams createOpenOrdersParams() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void verifyOrder(LimitOrder limitOrder) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void verifyOrder(MarketOrder marketOrder) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Order> getOrder(String... orderIds) throws IOException {
    updateAgainstMarket();
    final Set<Long> ids = Arrays.asList(orderIds).stream().map(Long::valueOf).collect(Collectors.toSet());
    return openOrders.entrySet().stream().filter(e -> ids.contains(e.getKey())).map(Entry::getValue).collect(Collectors.toList());
  }

  /**
   * Mimics reality by ensuring some operations have an indeterminate completion time,
   * so trades might be out of date.
   */
  private void randomDelay() {
    try {
      Thread.sleep(random.nextInt(2000));
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  /**
   * Checks to see if the market would have filled an order by looking at recent fills.
   */
  private synchronized void updateAgainstMarket() throws IOException {

    for (final Map.Entry<Long, LimitOrder> entry : openOrders.entrySet()) {

      final LimitOrder order = entry.getValue();

      // First just quickly see if the market has overtaken the order. If so,
      // no need to actually check orders.
      final Ticker ticker = marketDataService.getTicker(order.getCurrencyPair());
      switch (order.getType()) {
        case ASK:
          if (ticker.getBid().compareTo(order.getLimitPrice()) >= 0) {
            order.setCumulativeAmount(order.getOriginalAmount());
            order.setOrderStatus(Order.OrderStatus.FILLED);
            continue;
          }
          break;
        case BID:
          if (ticker.getAsk().compareTo(order.getLimitPrice()) <= 0) {
            order.setCumulativeAmount(order.getOriginalAmount());
            order.setOrderStatus(Order.OrderStatus.FILLED);
            continue;
          }
          break;
        default:
          throw new UnsupportedOperationException("Derivatives not supported");
      }

      final Trades trades = marketDataService.getTrades(order.getCurrencyPair());

      // Trades in the opposite direction to our order and which occurred after
      // we last checked and after the date of our order.
      Stream<Trade> tradeStream = trades.getTrades().stream()
        .filter(t -> tradeCanMatchOrder(t, order))
        .filter(t -> t.getTimestamp().after(placedDates.get(entry.getKey())))
        .filter(t -> lastMarketUpdate.before(t.getTimestamp()))
        .peek(t -> {
          lastMarketUpdate = t.getTimestamp();
        });

      // And which satisfy our price...
      switch (order.getType()) {
        case ASK:
          tradeStream = tradeStream.filter(t -> t.getPrice().compareTo(order.getLimitPrice()) >= 0);
          break;
        case BID:
          tradeStream = tradeStream.filter(t -> t.getPrice().compareTo(order.getLimitPrice()) <= 0);
          break;
        default:
          throw new UnsupportedOperationException("Derivatives not supported");
      }

      // Apply each one to the orders
      tradeStream.forEach(t -> {
        if (t.getOriginalAmount().compareTo(order.getRemainingAmount()) >= 0) {
          order.setCumulativeAmount(order.getOriginalAmount());
          order.setOrderStatus(Order.OrderStatus.FILLED);
        } else {
          order.setCumulativeAmount(order.getCumulativeAmount().add(t.getOriginalAmount()));
          order.setOrderStatus(Order.OrderStatus.PARTIALLY_FILLED);
        }
        // We assume a limit order
        order.setAveragePrice(order.getLimitPrice());
      });
    }
  }


  private boolean tradeCanMatchOrder(Trade trade, LimitOrder order) {
    switch (order.getType()) {
      case ASK: return trade.getType().equals(OrderType.BID);
      case BID: return trade.getType().equals(OrderType.ASK);
      default:
        throw new UnsupportedOperationException("Derivatives not supported");
    }
  }


  public static class Factory implements TradeServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Factory.class);

    private final ExchangeService exchangeService;

    private final LoadingCache<String, TradeService> services = CacheBuilder.newBuilder().initialCapacity(1000).build(new CacheLoader<String, TradeService>() {
      @Override
      public TradeService load(String exchange) throws Exception {
        LOGGER.warn("No API connection details.  Using paper trading.");
        return new PaperTradeService(exchangeService.get(exchange).getMarketDataService());
      }
    });

    @Inject
    Factory(ExchangeService exchangeService) {
      this.exchangeService = exchangeService;
    }

    @Override
    public TradeService getForExchange(String exchange) {
      return services.getUnchecked(exchange);
    }
  }
}