package com.grahamcrockford.oco.marketdata;

import static com.grahamcrockford.oco.marketdata.MarketDataType.OPEN_ORDERS;
import static com.grahamcrockford.oco.marketdata.MarketDataType.ORDERBOOK;
import static com.grahamcrockford.oco.marketdata.MarketDataType.TICKER;
import static com.grahamcrockford.oco.marketdata.MarketDataType.TRADES;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.OpenOrders;
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
import com.google.common.eventbus.EventBus;
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
import io.reactivex.disposables.Disposable;

/**
 * Maintains subscriptions to exchange market data, distributing them via the event bus.
 */
@Singleton
@VisibleForTesting
public class MarketDataSubscriptionManager extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataSubscriptionManager.class);

  private final EventBus eventBus;
  private final ExchangeService exchangeService;
  private final Sleep sleep;

  private final Multimap<String, Disposable> subsPerExchange = HashMultimap.create();
  private final Multimap<String, MarketDataSubscription> subscriptionsPerExchange = HashMultimap.create();

  private volatile ImmutableSet<MarketDataSubscription> activePolling = ImmutableSet.of();

  private final TradeServiceFactory tradeServiceFactory;


  @Inject
  @VisibleForTesting
  public MarketDataSubscriptionManager(EventBus eventBus, ExchangeService exchangeService, Sleep sleep, TradeServiceFactory tradeServiceFactory) {
    this.exchangeService = exchangeService;
    this.eventBus = eventBus;
    this.sleep = sleep;
    this.tradeServiceFactory = tradeServiceFactory;
  }

  /**
   * Updates the subscriptions for the specified exchanges.
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
      subsPerExchange.removeAll(exchangeName);
      subscriptionsPerExchange.removeAll(exchangeName);
    });

    return unchanged.build();
  }

  private void disconnectExchange(String exchangeName) {
    Collection<Disposable> oldSubs = subsPerExchange.get(exchangeName);
    if (!oldSubs.isEmpty()) {
      LOGGER.info("Disconnecting from exchange: " + exchangeName);
      StreamingExchange exchange = (StreamingExchange) exchangeService.get(exchangeName);
      exchange.disconnect().blockingAwait(); // Seems odd but this avoids an NPE when unsubscribing
      if (!oldSubs.isEmpty()) {
        oldSubs.forEach(Disposable::dispose);
      }
      LOGGER.info("Disconnected from exchange: " + exchangeName);
    }
  }

  private void subscribe(Multimap<String, MarketDataSubscription> byExchange, Set<String> unchanged) {
    final Builder<MarketDataSubscription> pollingBuilder = ImmutableSet.builder();
    byExchange.asMap()
      .entrySet()
      .stream()
      .forEach(entry -> {
        Exchange exchange = exchangeService.get(entry.getKey());
        Collection<MarketDataSubscription> subscriptionsForExchange = entry.getValue();
        boolean streaming = exchange instanceof StreamingExchange;
        if (streaming) {
          if (!unchanged.contains(entry.getKey())) {
            Collection<MarketDataSubscription> streamingSubscriptions = FluentIterable.from(entry.getValue()).filter(s -> !s.type().equals(OPEN_ORDERS)).toSet();
            subscriptionsPerExchange.putAll(entry.getKey(), streamingSubscriptions);
            subscribeExchange((StreamingExchange)exchange, streamingSubscriptions, entry.getKey());
          }
          pollingBuilder.addAll(FluentIterable.from(entry.getValue()).filter(s -> s.type().equals(OPEN_ORDERS)).toSet());
        } else {
          pollingBuilder.addAll(subscriptionsForExchange);
        }
      });
    activePolling = pollingBuilder.build();
    LOGGER.info("Polls now set to: " + activePolling);
  }

  private void subscribeExchange(StreamingExchange streamingExchange, Collection<MarketDataSubscription> subscriptionsForExchange, String exchangeName) {
    if (subscriptionsForExchange.isEmpty())
      return;
    LOGGER.info("Connecting to exchange: " + exchangeName);
    openConnections(streamingExchange, subscriptionsForExchange);
    LOGGER.info("Connected to exchange: " + exchangeName);
  }

  private void openConnections(StreamingExchange streamingExchange, Collection<MarketDataSubscription> subscriptionsForExchange) {

    // Connect, specifying all the subscriptions we need
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

    // Add the subscriptions
    StreamingMarketDataService marketDataService = streamingExchange.getStreamingMarketDataService();
    subscriptionsForExchange.stream()
      .forEach(s -> {
        if (s.type().equals(TICKER)) {
          Disposable subscription = marketDataService
              .getTicker(s.spec().currencyPair())
              .throttleLast(250, TimeUnit.MILLISECONDS) // TODO TEMPORARY.  Need to implement this throttling downstream at the websocket.
              .subscribe(
                ticker -> onTicker(s.spec(), ticker),
                throwable -> LOGGER.error("Error in subscribing tickers.", throwable)
              );
          subsPerExchange.put(s.spec().exchange(), subscription);
        }
        if (s.type().equals(ORDERBOOK)) {
          Disposable subscription = marketDataService
              .getOrderBook(s.spec().currencyPair())
              .throttleLast(2, TimeUnit.SECONDS) // TODO TEMPORARY.  Need to implement this throttling downstream at the websocket.
              .subscribe(
                orderBook -> onOrderBook(s.spec(), orderBook),
                throwable -> LOGGER.error("Error in subscribing order book.", throwable)
              );
          subsPerExchange.put(s.spec().exchange(), subscription);
        }
        if (s.type().equals(TRADES)) {
          Disposable subscription = marketDataService
              .getTrades(s.spec().currencyPair())
              .throttleLast(1, TimeUnit.SECONDS) // TODO TEMPORARY.  Need to implement this throttling downstream at the websocket.
              .subscribe(
                trade -> onTrade(s.spec(), trade),
                throwable -> LOGGER.error("Error in subscribing trades.", throwable)
              );
          subsPerExchange.put(s.spec().exchange(), subscription);
        }
      });
  }

  private void onTicker(TickerSpec spec, Ticker ticker) {
    LOGGER.debug("Got ticker {} on {}", ticker, spec);
    eventBus.post(TickerEvent.create(spec, ticker));
  }

  private void onTrade(TickerSpec spec, Trade trade) {
    LOGGER.info("Got trade {} on {}", trade, spec);
    eventBus.post(TradeEvent.create(spec, trade));
  }

  private void onOrderBook(TickerSpec spec, OrderBook orderBook) {
    LOGGER.debug("Got orderBook {} on {}", orderBook, spec);
    eventBus.post(OrderBookEvent.create(spec, orderBook));
  }

  private void onOpenOrders(TickerSpec spec, OpenOrders openOrders) {
    LOGGER.debug("Got open orders {} on {}", openOrders, spec);
    eventBus.post(OpenOrdersEvent.create(spec, openOrders));
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
    LOGGER.info(this + " stopped");
  }

  private void fetchAndBroadcast(MarketDataSubscription subscription) {
    try {
      TickerSpec spec = subscription.spec();
      MarketDataService marketDataService = exchangeService.get(spec.exchange()).getMarketDataService();
      if (subscription.type().equals(TICKER)) {
        onTicker(spec, marketDataService.getTicker(spec.currencyPair()));
      } else if (subscription.type().equals(ORDERBOOK)) {
        onOrderBook(spec, marketDataService.getOrderBook(spec.currencyPair()));
      } else if (subscription.type().equals(TRADES)) {
        marketDataService.getTrades(spec.currencyPair())
          .getTrades()
          .stream()
          .forEach(t -> onTrade(spec, t));
      } else if (subscription.type().equals(OPEN_ORDERS)) {
        TradeService tradeService = tradeServiceFactory.getForExchange(subscription.spec().exchange());
        onOpenOrders(spec, tradeService.getOpenOrders(new DefaultOpenOrdersParamCurrencyPair(subscription.spec().currencyPair())));
      }
    } catch (Throwable e) {
      LOGGER.error("Error fetching market data: " + subscription, e);
    }
  }
}