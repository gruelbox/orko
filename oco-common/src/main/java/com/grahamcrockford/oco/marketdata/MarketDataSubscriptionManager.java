package com.grahamcrockford.oco.marketdata;

import static com.grahamcrockford.oco.marketdata.MarketDataType.OPEN_ORDERS;
import static com.grahamcrockford.oco.marketdata.MarketDataType.ORDERBOOK;
import static com.grahamcrockford.oco.marketdata.MarketDataType.TICKER;
import static com.grahamcrockford.oco.marketdata.MarketDataType.TRADES;
import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Set;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.exchange.ExchangeService;
import com.grahamcrockford.oco.exchange.TradeServiceFactory;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.util.Sleep;

import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.ProductSubscription.ProductSubscriptionBuilder;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.disposables.Disposable;

/**
 * Maintains subscriptions to exchange market data, distributing them via the event bus.
 */
@Singleton
@VisibleForTesting
public class MarketDataSubscriptionManager extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataSubscriptionManager.class);

  private final ExchangeService exchangeService;
  private final TradeServiceFactory tradeServiceFactory;
  private final Sleep sleep;

  private final Multimap<String, MarketDataSubscription> subscriptionsPerExchange = HashMultimap.create();
  private volatile ImmutableSet<MarketDataSubscription> activePolling = ImmutableSet.of();
  private final Multimap<String, Disposable> disposablesPerExchange = HashMultimap.create();

  private final Flowable<TickerEvent> tickers;
  private final AtomicReference<FlowableEmitter<TickerEvent>> tickerEmitter = new AtomicReference<>();
  private final Flowable<OpenOrdersEvent> openOrders;
  private final AtomicReference<FlowableEmitter<OpenOrdersEvent>> openOrdersEmitter = new AtomicReference<>();
  private final Flowable<OrderBookEvent> orderbook;
  private final AtomicReference<FlowableEmitter<OrderBookEvent>> orderBookEmitter = new AtomicReference<>();
  private final Flowable<TradeEvent> trades;
  private final AtomicReference<FlowableEmitter<TradeEvent>> tradesEmitter = new AtomicReference<>();


  @Inject
  @VisibleForTesting
  public MarketDataSubscriptionManager(ExchangeService exchangeService, Sleep sleep, TradeServiceFactory tradeServiceFactory) {
    this.exchangeService = exchangeService;
    this.sleep = sleep;
    this.tradeServiceFactory = tradeServiceFactory;
    this.tickers = Flowable.create((FlowableEmitter<TickerEvent> e) -> tickerEmitter.set(e.serialize()), BackpressureStrategy.MISSING).share().onBackpressureLatest();
    this.openOrders = Flowable.create((FlowableEmitter<OpenOrdersEvent> e) -> openOrdersEmitter.set(e.serialize()), BackpressureStrategy.MISSING).share().onBackpressureLatest();
    this.orderbook = Flowable.create((FlowableEmitter<OrderBookEvent> e) -> orderBookEmitter.set(e.serialize()), BackpressureStrategy.MISSING).share().onBackpressureLatest();
    this.trades = Flowable.create((FlowableEmitter<TradeEvent> e) -> tradesEmitter.set(e.serialize()), BackpressureStrategy.MISSING).share().onBackpressureLatest();
  }


  /**
   * Updates the subscriptions for the specified exchanges.  Call with an empty set
   * to cancel all subscriptions.  None of the streams (e.g. {@link #getTicker(TickerSpec)}
   * will return anything until this is called, but there is no strict order
   * in which they need to be called.
   *
   * @param byExchange The exchanges and subscriptions for each.
   */
  public synchronized void updateSubscriptions(Set<MarketDataSubscription> subscriptions) {
    LOGGER.info("Updating subscriptions to: " + subscriptions);
    Multimap<String, MarketDataSubscription> byExchange = Multimaps.<String, MarketDataSubscription>index(subscriptions, sub -> sub.spec().exchange());

    // Disconnect any streaming exchanges where the tickers currently
    // subscribed mismatch the ones we want.
    Set<String> unchanged = disconnectChangedExchanges(byExchange);

    // Add new subscriptions
    subscribe(byExchange, unchanged);
  }


  /**
   * Gets the stream of a subscription.  Typed by the caller in
   * an unsafe manner for convenience,
   *
   * @param sub The subscription
   * @return The stream.
   */
  @SuppressWarnings("unchecked")
  public <T> Flowable<T> getSubscription(MarketDataSubscription sub) {
    switch (sub.type()) {
      case OPEN_ORDERS:
        return (Flowable<T>) getOpenOrders(sub.spec());
      case ORDERBOOK:
        return (Flowable<T>) getOrderBook(sub.spec());
      case TICKER:
        return (Flowable<T>) getTicker(sub.spec());
      case TRADES:
        return (Flowable<T>) getTrades(sub.spec());
      default:
        throw new IllegalArgumentException("Unknown market data type");
    }
  }


  /**
   * Gets a stream of tickers.
   *
   * @param spec The ticker specification.
   * @return The ticker stream.
   */
  public Flowable<TickerEvent> getTicker(TickerSpec spec) {
    return tickers.filter(t -> t.spec().equals(spec))
      .doOnNext(t -> {
        if (LOGGER.isDebugEnabled()) logTicker("filtered", t);
      });
  }

  public Flowable<OpenOrdersEvent> getOpenOrders(TickerSpec spec) {
    return openOrders.filter(t -> t.spec().equals(spec));
  }

  public Flowable<OrderBookEvent> getOrderBook(TickerSpec spec) {
    return orderbook.filter(t -> t.spec().equals(spec));
  }

  public Flowable<TradeEvent> getTrades(TickerSpec spec) {
    return trades.filter(t -> t.spec().equals(spec));
  }


  private Set<String> disconnectChangedExchanges(Multimap<String, MarketDataSubscription> byExchange) {
    Builder<String> unchanged = ImmutableSet.builder();

    List<String> changed = Lists.newArrayListWithCapacity(subscriptionsPerExchange.keySet().size());

    for (Entry<String, Collection<MarketDataSubscription>> entry : subscriptionsPerExchange.asMap().entrySet()) {

      String exchangeName = entry.getKey();
      Collection<MarketDataSubscription> current = entry.getValue();
      Collection<MarketDataSubscription> target = FluentIterable.from(byExchange.get(exchangeName)).filter(s -> !s.type().equals(OPEN_ORDERS)).toSet();

      LOGGER.info("Exchange {} has {}, wants {}", exchangeName, current, target);

      if (current.equals(target)) {
        unchanged.add(exchangeName);
      } else {
        changed.add(exchangeName);
      }
    }

    changed.forEach(exchangeName -> {
      LOGGER.info("... disconnecting " + exchangeName);
      disconnectExchange(exchangeName);
      subscriptionsPerExchange.removeAll(exchangeName);
    });

    return unchanged.build();
  }

  private void disconnectExchange(String exchangeName) {
    LOGGER.info("Disconnecting from exchange: " + exchangeName);
    StreamingExchange exchange = (StreamingExchange) exchangeService.get(exchangeName);

    disposablesPerExchange.get(exchangeName).forEach(d -> {
      try {
        d.dispose();
      } catch (Exception e) {
        LOGGER.error("Error disposing of subscription", e);
      }
    });
    disposablesPerExchange.removeAll(exchangeName);

    exchange.disconnect().blockingAwait();

    LOGGER.info("Disconnected from exchange: " + exchangeName);
  }

  private void subscribe(Multimap<String, MarketDataSubscription> byExchange, Set<String> unchanged) {
    final Builder<MarketDataSubscription> pollingBuilder = ImmutableSet.builder();
    byExchange.asMap()
      .entrySet()
      .stream()
      .forEach(entry -> {
        Exchange exchange = exchangeService.get(entry.getKey());
        Collection<MarketDataSubscription> subscriptionsForExchange = entry.getValue();
        if (isStreamingExchange(exchange)) {
          if (!unchanged.contains(entry.getKey())) {
            Collection<MarketDataSubscription> streamingSubscriptions = FluentIterable.from(entry.getValue()).filter(s -> !s.type().equals(OPEN_ORDERS)).toSet();
            subscriptionsPerExchange.putAll(entry.getKey(), streamingSubscriptions);
            subscribeExchange((StreamingExchange)exchange, streamingSubscriptions, entry.getKey());
            StreamingMarketDataService streaming = ((StreamingExchange)exchange).getStreamingMarketDataService();
            disposablesPerExchange.putAll(
              entry.getKey(),
              FluentIterable.from(streamingSubscriptions).transform(sub -> {
                switch (sub.type()) {
                  case ORDERBOOK:
                    return streaming.getOrderBook(sub.spec().currencyPair())
                        .map(t -> OrderBookEvent.create(sub.spec(), t))
                        .subscribe(this::onOrderBook);
                  case TICKER:
                    LOGGER.debug("Subscribing to {}", sub.spec());
                    return streaming.getTicker(sub.spec().currencyPair())
                        .map(t -> TickerEvent.create(sub.spec(), t))
                        .subscribe(this::onTicker);
                  case TRADES:
                    return streaming.getTrades(sub.spec().currencyPair())
                        .map(t -> TradeEvent.create(sub.spec(), t))
                        .subscribe(this::onTrade);
                  default:
                    throw new IllegalStateException("Unexpected market data type: " + sub.type());
                }
              })
            );
          }
          pollingBuilder.addAll(FluentIterable.from(entry.getValue()).filter(s -> s.type().equals(OPEN_ORDERS)).toSet());
        } else {
          pollingBuilder.addAll(subscriptionsForExchange);
        }
      });
    activePolling = pollingBuilder.build();
    LOGGER.info("Polls now set to: " + activePolling);
  }

  private void onTicker(TickerEvent e) {
    logTicker("onTicker", e);
    if (tickerEmitter.get() != null)
      tickerEmitter.get().onNext(e);
  }

  private void onTrade(TradeEvent e) {
    if (tradesEmitter.get() != null)
      tradesEmitter.get().onNext(e);
  }

  private void onOrderBook(OrderBookEvent e) {
    if (orderBookEmitter.get() != null)
      orderBookEmitter.get().onNext(e);
  }

  private void onOpenOrders(OpenOrdersEvent e) {
    if (openOrdersEmitter.get() != null)
      openOrdersEmitter.get().onNext(e);
  }

  private void subscribeExchange(StreamingExchange streamingExchange, Collection<MarketDataSubscription> subscriptionsForExchange, String exchangeName) {
    if (subscriptionsForExchange.isEmpty())
      return;
    LOGGER.info("Connecting to exchange: " + exchangeName);
    openConnections(streamingExchange, subscriptionsForExchange);
    LOGGER.info("Connected to exchange: " + exchangeName);
  }

  private void openConnections(StreamingExchange streamingExchange, Collection<MarketDataSubscription> subscriptionsForExchange) {
    ProductSubscriptionBuilder builder = ProductSubscription.create();
    subscriptionsForExchange.stream()
      .forEach(s -> {
        if (s.type().equals(TICKER)) {
          builder.addTicker(s.spec().currencyPair());
        }
        if (s.type().equals(ORDERBOOK)) {
          builder.addOrderbook(s.spec().currencyPair());
        }
        if (s.type().equals(TRADES)) {
          builder.addTrades(s.spec().currencyPair());
        }
      });
    streamingExchange.connect(builder.build()).blockingAwait();
  }

  @Override
  protected void run() {
    Thread.currentThread().setName("Market data subscription manager");
    LOGGER.info(this + " started");
    while (isRunning()) {
      activePolling.forEach(this::fetchAndBroadcast);
      try {
        sleep.sleep();
      } catch (InterruptedException e) {
        break;
      }
    }
    updateSubscriptions(emptySet());
    LOGGER.info(this + " stopped");
  }

  private void fetchAndBroadcast(MarketDataSubscription subscription) {
    try {
      TickerSpec spec = subscription.spec();
      MarketDataService marketDataService = exchangeService.get(spec.exchange()).getMarketDataService();
      if (subscription.type().equals(TICKER)) {
        onTicker(TickerEvent.create(spec, marketDataService.getTicker(spec.currencyPair())));
      } else if (subscription.type().equals(ORDERBOOK)) {
        onOrderBook(OrderBookEvent.create(spec, marketDataService.getOrderBook(spec.currencyPair())));
      } else if (subscription.type().equals(TRADES)) {
        throw new UnsupportedOperationException("TODO"); // TODO
//        marketDataService.getTrades(spec.currencyPair())
//          .getTrades()
//          .stream()
//          .forEach(t -> onTrade(spec, t));
      } else if (subscription.type().equals(OPEN_ORDERS)) {
        TradeService tradeService = tradeServiceFactory.getForExchange(subscription.spec().exchange());
        onOpenOrders(OpenOrdersEvent.create(spec, tradeService.getOpenOrders(new DefaultOpenOrdersParamCurrencyPair(subscription.spec().currencyPair()))));
      }
    } catch (Throwable e) {
      LOGGER.error("Error fetching market data: " + subscription, e);
    }
  }

  private boolean isStreamingExchange(Exchange exchange) {
    return exchange instanceof StreamingExchange;
  }

  private void logTicker(String context, TickerEvent e) {
    LOGGER.debug("Ticker [{}] ({}) {}/{} = {}", context, e.spec().exchange(), e.spec().base(), e.spec().counter(), e.ticker().getLast());
  }
}