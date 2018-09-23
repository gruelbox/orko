package com.grahamcrockford.oco.marketdata;

import static com.grahamcrockford.oco.marketdata.MarketDataType.BALANCE;
import static com.grahamcrockford.oco.marketdata.MarketDataType.ORDERBOOK;
import static com.grahamcrockford.oco.marketdata.MarketDataType.TICKER;
import static com.grahamcrockford.oco.marketdata.MarketDataType.TRADES;
import static java.util.Collections.emptySet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.TradeHistoryParamCurrencyPair;
import org.knowm.xchange.service.trade.params.TradeHistoryParamLimit;
import org.knowm.xchange.service.trade.params.TradeHistoryParamPaging;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.exchange.AccountServiceFactory;
import com.grahamcrockford.oco.exchange.ExchangeService;
import com.grahamcrockford.oco.exchange.TradeServiceFactory;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.util.SafelyDispose;

import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.ProductSubscription.ProductSubscriptionBuilder;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Maintains subscriptions to multiple exchanges' market data, using web sockets where it can
 * and polling where it can't, but this is abstracted away. All clients have access to reactive
 * streams of data to which they are subscribed.
 */
@Singleton
@VisibleForTesting
public class MarketDataSubscriptionManager extends AbstractExecutionThreadService {

  private static final int MAX_TRADES = 20;
  private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataSubscriptionManager.class);
  private static final int ORDERBOOK_DEPTH = 20;
  private static final Set<MarketDataType> STREAMING_MARKET_DATA = ImmutableSet.of(TICKER, TRADES, ORDERBOOK);

  private final ExchangeService exchangeService;
  private final TradeServiceFactory tradeServiceFactory;
  private final AccountServiceFactory accountServiceFactory;
  private final OcoConfiguration configuration;
  private final EventBus eventBus;

  private final Map<String, AtomicReference<Set<MarketDataSubscription>>> nextSubscriptions;
  private final ConcurrentMap<String, Set<MarketDataSubscription>> subscriptionsPerExchange = Maps.newConcurrentMap();
  private final ConcurrentMap<String, Set<MarketDataSubscription>> pollsPerExchange = Maps.newConcurrentMap();
  private final Multimap<String, Disposable> disposablesPerExchange = HashMultimap.create();
  private final Set<MarketDataSubscription> unavailableSubscriptions = Sets.newConcurrentHashSet();

  private final CachingSubscription<TickerEvent, TickerSpec> tickers;
  private final CachingSubscription<OpenOrdersEvent, TickerSpec> openOrders;
  private final CachingSubscription<OrderBookEvent, TickerSpec> orderbook;
  private final CachingSubscription<TradeEvent, TickerSpec> trades;
  private final CachingSubscription<TradeHistoryEvent, TickerSpec> tradeHistory;
  private final CachingSubscription<BalanceEvent, String> balance;

  private final Phaser phaser  = new Phaser(1);


  @Inject
  @VisibleForTesting
  public MarketDataSubscriptionManager(ExchangeService exchangeService, OcoConfiguration configuration, TradeServiceFactory tradeServiceFactory, AccountServiceFactory accountServiceFactory, EventBus eventBus) {
    this.exchangeService = exchangeService;
    this.configuration = configuration;
    this.tradeServiceFactory = tradeServiceFactory;
    this.accountServiceFactory = accountServiceFactory;
    this.eventBus = eventBus;

    this.nextSubscriptions = FluentIterable.from(exchangeService.getExchanges())
        .toMap(e -> new AtomicReference<Set<MarketDataSubscription>>());

    exchangeService.getExchanges().forEach(e -> {
      subscriptionsPerExchange.put(e, ImmutableSet.of());
      pollsPerExchange.put(e, ImmutableSet.of());
    });

    this.tickers = new CachingSubscription<>(TickerEvent::spec);
    this.openOrders = new CachingSubscription<>(OpenOrdersEvent::spec);
    this.orderbook = new CachingSubscription<>(OrderBookEvent::spec);
    this.trades = new CachingSubscription<>(TradeEvent::spec);
    this.tradeHistory = new CachingSubscription<>(TradeHistoryEvent::spec);
    this.balance = new CachingSubscription<>((BalanceEvent e) -> e.exchange() + "/" + e.currency());
  }


  /**
   * Updates the subscriptions for the specified exchanges on the next loop
   * tick. The delay is to avoid a large number of new subscriptions in quick
   * succession causing rate bans on exchanges. Call with an empty set to cancel
   * all subscriptions. None of the streams (e.g. {@link #getTicker(TickerSpec)}
   * will return anything until this is called, but there is no strict order in
   * which they need to be called.
   *
   * @param byExchange The exchanges and subscriptions for each.
   */
  public void updateSubscriptions(Set<MarketDataSubscription> subscriptions) {

    // Queue them up for each exchange's processing thread individually
    ImmutableListMultimap<String, MarketDataSubscription> byExchange = Multimaps.index(subscriptions, s -> s.spec().exchange());
    for (String exchangeName : exchangeService.getExchanges()) {
      nextSubscriptions.get(exchangeName).set(ImmutableSet.copyOf(byExchange.get(exchangeName)));
    }

    // Give the loops a kick
    int phase = phaser.arrive();
    LOGGER.debug("Progressing to phase {}", phase);
  }


  /**
   * Gets the stream of a subscription.  Typed by the caller in
   * an unsafe manner for convenience.
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
      case USER_TRADE_HISTORY:
        return (Flowable<T>) getTradeHistory(sub.spec());
      case BALANCE:
        return (Flowable<T>) getBalance(sub.spec());
      default:
        throw new IllegalArgumentException("Unknown market data type");
    }
  }


  /**
   * Gets a stream of tickers, starting with any cached tickers.
   *
   * @param spec The ticker specification.
   * @return The ticker stream.
   */
  public Flowable<TickerEvent> getTicker(TickerSpec spec) {
    return tickers.get(spec);
  }


  /**
   * Gets a stream of open order lists.
   *
   * @param spec The ticker specification.
   */
  public Flowable<OpenOrdersEvent> getOpenOrders(TickerSpec spec) {
    return openOrders.get(spec);
  }


  /**
   * Gets a stream containing updates to the order book.
   *
   * @param spec The ticker specification.
   */
  public Flowable<OrderBookEvent> getOrderBook(TickerSpec spec) {
    return orderbook.get(spec);
  }


  /**
   * Gets a stream of trades.
   *
   * @param spec The ticker specification.
   */
  public Flowable<TradeEvent> getTrades(TickerSpec spec) {
    return trades.get(spec);
  }


  /**
   * Gets a stream with updates to the recent trade history.
   *
   * @param spec The ticker specification.
   */
  public Flowable<TradeHistoryEvent> getTradeHistory(TickerSpec spec) {
    return tradeHistory.get(spec);
  }


  /**
   * Gets a stream with updates to the balance.
   *
   * @param spec The ticker specification.
   */
  public Flowable<BalanceEvent> getBalance(TickerSpec spec) {
    return balance.get(
      spec.exchange() + "/" + spec.base(),
      spec.exchange() + "/" + spec.counter()
    );
  }


  /**
   * Actually performs the subscription changes. Occurs synchronously in the
   * poll loop.
   */
  private boolean doSubscriptionChanges(String exchangeName) throws Exception {
    Set<MarketDataSubscription> subscriptions = nextSubscriptions.get(exchangeName).getAndSet(null);
    if (subscriptions == null)
      return false;
    try {

      LOGGER.info("Updating {} subscriptions to: {}", exchangeName, subscriptions);

      // Remember all our old subscriptions for a moment...
      Set<MarketDataSubscription> oldSubscriptions = FluentIterable.from(Iterables.concat(
          subscriptionsPerExchange.get(exchangeName),
          pollsPerExchange.get(exchangeName)
        ))
        .toSet();

      // Disconnect any streaming exchanges where the tickers currently
      // subscribed mismatch the ones we want.
      if (subscriptions.equals(oldSubscriptions)) {
        return false;
      } else {

        if (!oldSubscriptions.isEmpty()) {
          disconnectExchange(exchangeName);
        }

        // Clear cached tickers and order books for anything we've unsubscribed so that we don't feed out-of-date data
        Sets.difference(oldSubscriptions, subscriptions)
          .forEach(s -> {
            tickers.removeFromCache(s.spec());
            orderbook.removeFromCache(s.spec());
            openOrders.removeFromCache(s.spec());
            trades.removeFromCache(s.spec());
            tradeHistory.removeFromCache(s.spec());
            balance.removeFromCache(s.spec().exchange() + "/" + s.spec().base());
            balance.removeFromCache(s.spec().exchange() + "/" + s.spec().counter());
          });

        // Add new subscriptions if we have any
        if (!subscriptions.isEmpty()) {
          subscribe(exchangeName, subscriptions);
        }

        return true;

      }

    } catch (Exception e) {
      LOGGER.error("Error updating subscriptions", e);
      if (nextSubscriptions.get(exchangeName).compareAndSet(null, subscriptions)) {
        int phase = phaser.arrive();
        LOGGER.debug("Progressing to phase {}", phase);
      }
      throw e;
    }
  }

  private void disconnectExchange(String exchangeName) {
    Exchange exchange = exchangeService.get(exchangeName);
    if (exchange instanceof StreamingExchange) {
      SafelyDispose.of(disposablesPerExchange.removeAll(exchangeName));
      ((StreamingExchange) exchange).disconnect().blockingAwait();
    }
  }

  private void subscribe(String exchangeName, Set<MarketDataSubscription> subscriptions) {

    Builder<MarketDataSubscription> pollingBuilder = ImmutableSet.builder();

    Exchange exchange = exchangeService.get(exchangeName);

    if (isStreamingExchange(exchange)) {
      Set<MarketDataSubscription> streamingSubscriptions = FluentIterable.from(subscriptions).filter(s -> STREAMING_MARKET_DATA.contains(s.type())).toSet();
      if (!streamingSubscriptions.isEmpty()) {
        openSubscriptions(exchangeName, exchange, streamingSubscriptions);
      }
      pollingBuilder.addAll(FluentIterable.from(subscriptions).filter(s -> !STREAMING_MARKET_DATA.contains(s.type())).toSet());
    } else {
      pollingBuilder.addAll(subscriptions);
    }

    Set<MarketDataSubscription> polls = pollingBuilder.build();
    pollsPerExchange.put(exchangeName, pollingBuilder.build());
    LOGGER.debug("Polls now set to: {}", polls);
  }


  private void openSubscriptions(String exchangeName, Exchange exchange, Set<MarketDataSubscription> streamingSubscriptions) {
    subscriptionsPerExchange.put(exchangeName, streamingSubscriptions);
    subscribeExchange((StreamingExchange)exchange, streamingSubscriptions, exchangeName);

    StreamingMarketDataService streaming = ((StreamingExchange)exchange).getStreamingMarketDataService();
    disposablesPerExchange.putAll(
      exchangeName,
      FluentIterable.from(streamingSubscriptions).transform(sub -> {
        switch (sub.type()) {
          case ORDERBOOK:
            return streaming.getOrderBook(sub.spec().currencyPair())
                .map(t -> OrderBookEvent.create(sub.spec(), t))
                .subscribe(orderbook::emit, e -> LOGGER.error("Error in order book stream for " + sub, e));
          case TICKER:
            LOGGER.debug("Subscribing to {}", sub.spec());
            return streaming.getTicker(sub.spec().currencyPair())
                .map(t -> TickerEvent.create(sub.spec(), t))
                .subscribe(tickers::emit, e -> LOGGER.error("Error in ticker stream for " + sub, e));
          case TRADES:
            return streaming.getTrades(sub.spec().currencyPair())
                .map(t -> TradeEvent.create(sub.spec(), t))
                .subscribe(trades::emit, e -> LOGGER.error("Error in trade stream for " + sub, e));
          default:
            throw new IllegalStateException("Unexpected market data type: " + sub.type());
        }
      })
    );
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
    LOGGER.info("{} started", this);
    ForkJoinPool forkJoinPool = new ForkJoinPool(exchangeService.getExchanges().size() + 1);
    try {
      forkJoinPool.submit(() ->
        exchangeService.getExchanges()
          .stream()
          .parallel()
          .forEach(this::pollExchange)
      ).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return;
    } catch (ExecutionException e1) {
      throw new RuntimeException(e1);
    }
    updateSubscriptions(emptySet());
    LOGGER.info(this + " stopped");
  }

  @Override
  protected void triggerShutdown() {
    super.triggerShutdown();
    phaser.arriveAndDeregister();
    phaser.forceTermination();
  }

  private void pollExchange(String exchangeName) {
    Thread.currentThread().setName(getClass().getSimpleName() + "-" + exchangeName);
    long defaultSleep = configuration.getLoopSeconds() * 1000;
    while (!phaser.isTerminated()) {

      // Before we check for the presence of polls, determine which phase
      // we are going to wait for if there's no work to do - i.e. the
      // next wakeup.
      int phase = phaser.getPhase();
      if (phase == -1)
        break;

      // Handle any pending resubscriptions.
      LOGGER.debug("{} - start subscription check", exchangeName);
      boolean subscriptionsFailed = false;
      boolean resubscribed = false;
      try {
        resubscribed = doSubscriptionChanges(exchangeName);
      } catch (Exception e) {
        subscriptionsFailed = true;
      }

      // Work out how often we can poll the exchange safely.
      Set<MarketDataSubscription> polls = FluentIterable.from(pollsPerExchange.get(exchangeName))
          .filter(s -> !unavailableSubscriptions.contains(s)).toSet();
      long interApiSleep = sleepTime(exchangeName, defaultSleep, polls.isEmpty() ? 10 : polls.size());

      // Pause after a resubscription since it probably counts as an API call and thus
      // toward the rate limit
      if (resubscribed) {
        if (!sleep(exchangeName, interApiSleep)) {
          break;
        }
      }

      // Check if we have any polling to do. If not, go to sleep until awoken
      // by a subscription change, unless we failed to process subscriptions,
      // in which case wake ourselves up in a few seconds to try again
      if (polls.isEmpty()) {
        LOGGER.debug("{} - poll going to sleep", exchangeName);
        try {
          if (subscriptionsFailed) {
            phaser.awaitAdvanceInterruptibly(phase, defaultSleep, TimeUnit.MILLISECONDS);
          } else {
            LOGGER.debug("{} - sleeping until phase {}", exchangeName, phase); // TODO REMOVE
            phaser.awaitAdvanceInterruptibly(phase);
            LOGGER.debug("{} - poll woken up on request", exchangeName);
          }
          continue;
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        } catch (TimeoutException e) {
          continue;
        } catch (Exception e) {
          LOGGER.error("Failure in phaser wait for " + exchangeName, e);
          continue;
        }
      }

      LOGGER.debug("{} - start poll", exchangeName);
      Set<String> balanceCurrencies = new HashSet<>();
      for (MarketDataSubscription subscription : polls) {
        if (phaser.isTerminated())
          break;
        if (subscription.type().equals(BALANCE)) {
          balanceCurrencies.add(subscription.spec().base());
          balanceCurrencies.add(subscription.spec().counter());
        } else {
          fetchAndBroadcast(subscription);
          if (!sleep(exchangeName, interApiSleep))
            break;
        }
      }

      if (phaser.isTerminated())
        break;

      // We'll be extending this sort of batching to more market data types...
      if (!balanceCurrencies.isEmpty()) {
        try {
          fetchBalances(exchangeName, balanceCurrencies)
            .forEach(b -> balance.emit(BalanceEvent.create(exchangeName, b.currency(), b)));
        } catch (NotAvailableFromExchangeException e) {
          LOGGER.warn("{} not available on {}" , BALANCE, exchangeName);
          Iterables.addAll(unavailableSubscriptions, FluentIterable.from(polls).filter(s -> s.type().equals(BALANCE)));
        } catch (Throwable e) {
          LOGGER.error("Error fetching balance on " + exchangeName, e);
        }
        if (phaser.isTerminated())
          break;
        if (!sleep(exchangeName, interApiSleep))
          break;
      }

    }
  }

  private boolean sleep(String exchangeName, long sleepTime) {
    LOGGER.debug("{} pausing between API calls", exchangeName);
    try {
      Thread.sleep(sleepTime);
      return true;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private long sleepTime(String exchangeName, long defaultSleep, int pollCount) {
    try {
      return exchangeService.safePollDelay(exchangeName)
          .orElse(defaultSleep / pollCount);
    } catch (Exception e) {
      LOGGER.error("Failed to fetch exchange safe poll delay for " + exchangeName, e);
      return defaultSleep / pollCount;
    }
  }

  private Iterable<Balance> fetchBalances(String exchangeName, Collection<String> currencyCodes) throws IOException {
    return FluentIterable.from(exchangeWallet(exchangeName).getBalances().entrySet())
      .transform(Map.Entry::getValue)
      .filter(balance -> currencyCodes.contains(balance.getCurrency().getCurrencyCode()))
      .transform(Balance::create);
  }

  private Wallet exchangeWallet(String exchangeName) throws IOException {
    if (exchangeName.equals("bitfinex")) {
      return accountServiceFactory.getForExchange(exchangeName)
          .getAccountInfo()
          .getWallet("exchange");
    } else {
      return accountServiceFactory.getForExchange(exchangeName)
        .getAccountInfo()
        .getWallet();
    }
  }

  private void fetchAndBroadcast(MarketDataSubscription subscription) {
    try {
      TradeService tradeService;
      TickerSpec spec = subscription.spec();
      MarketDataService marketDataService = exchangeService.get(spec.exchange()).getMarketDataService();
      switch (subscription.type()) {
        case TICKER:
          tickers.emit(TickerEvent.create(spec, marketDataService.getTicker(spec.currencyPair())));
          break;
        case ORDERBOOK:
          if (spec.exchange().equals("cryptopia")) {
            // TODO submit a PR to xChange for this
            long longValue = Integer.valueOf(ORDERBOOK_DEPTH).longValue();
            orderbook.emit(OrderBookEvent.create(spec, marketDataService.getOrderBook(spec.currencyPair(), longValue, longValue)));
          } else {
            orderbook.emit(OrderBookEvent.create(spec, marketDataService.getOrderBook(spec.currencyPair(), ORDERBOOK_DEPTH, ORDERBOOK_DEPTH)));
          }
          break;
        case TRADES:
          // TODO need to return only the new ones onTrade(TradeEvent.create(spec, marketDataService.getTrades(spec.currencyPair())));
          throw new UnsupportedOperationException("Trades not supported yet");
        case OPEN_ORDERS:
          tradeService = tradeServiceFactory.getForExchange(subscription.spec().exchange());
          OpenOrdersParams openOrdersParams = openOrdersParams(subscription, tradeService);
          openOrders.emit(OpenOrdersEvent.create(spec, tradeService.getOpenOrders(openOrdersParams)));
          break;
        case USER_TRADE_HISTORY:
          tradeService = tradeServiceFactory.getForExchange(subscription.spec().exchange());
          TradeHistoryParams tradeHistoryParams = tradeHistoryParams(subscription, tradeService);
          ImmutableList<UserTrade> trades = ImmutableList.copyOf(tradeService.getTradeHistory(tradeHistoryParams).getUserTrades());
          tradeHistory.emit(TradeHistoryEvent.create(spec, trades));
          break;
        default:
          throw new IllegalStateException("Market data type " + subscription.type() + " not supported in this way");
      }
    } catch (NotAvailableFromExchangeException e) {
      LOGGER.warn(subscription.type() + " not available on " + subscription.spec().exchange());
      unavailableSubscriptions.add(subscription);
    } catch (Throwable e) {
      LOGGER.error("Error fetching market data: " + subscription, e);
    }
  }


  private TradeHistoryParams tradeHistoryParams(MarketDataSubscription subscription, TradeService tradeService) {
    TradeHistoryParams params;

    // TODO fix with pull request
    if (subscription.spec().exchange().startsWith("gdax")) {
      params = new TradeHistoryParamCurrencyPair() {

        private CurrencyPair pair;

        @Override
        public void setCurrencyPair(CurrencyPair pair) {
          this.pair = pair;
        }

        @Override
        public CurrencyPair getCurrencyPair() {
          return pair;
        }
      };
    } else {
      params = tradeService.createTradeHistoryParams();
    }

    if (params instanceof TradeHistoryParamCurrencyPair) {
      ((TradeHistoryParamCurrencyPair) params).setCurrencyPair(subscription.spec().currencyPair());
    } else {
      throw new UnsupportedOperationException("Don't know how to read user trades on this exchange: " + subscription.spec().exchange());
    }
    if (params instanceof TradeHistoryParamLimit) {
      ((TradeHistoryParamLimit) params).setLimit(MAX_TRADES);
    }
    if (params instanceof TradeHistoryParamPaging) {
      ((TradeHistoryParamPaging) params).setPageLength(MAX_TRADES);
      ((TradeHistoryParamPaging) params).setPageNumber(0);
    }
    return params;
  }

  private OpenOrdersParams openOrdersParams(MarketDataSubscription subscription, TradeService tradeService) {
    OpenOrdersParams params = tradeService.createOpenOrdersParams();
    if (params == null) {
      // Bitfinex
      params = new DefaultOpenOrdersParamCurrencyPair(subscription.spec().currencyPair());
    } else if (params instanceof OpenOrdersParamCurrencyPair) {
      ((OpenOrdersParamCurrencyPair) params).setCurrencyPair(subscription.spec().currencyPair());
    } else {
      throw new UnsupportedOperationException("Don't know how to read open orders on this exchange: " + subscription.spec().exchange());
    }
    return params;
  }

  private boolean isStreamingExchange(Exchange exchange) {
    return exchange instanceof StreamingExchange;
  }

  private final class CachingSubscription<T, U> {
    private final Flowable<T> flowable;
    private final AtomicReference<FlowableEmitter<T>> emitter = new AtomicReference<>();
    private final ConcurrentMap<U, T> latest = Maps.newConcurrentMap();
    private final Function<T, U> keyFunction;

    CachingSubscription(Function<T, U> keyFunction) {
      this.keyFunction = keyFunction;
      this.flowable = Flowable.create((FlowableEmitter<T> e) -> emitter.set(e.serialize()), BackpressureStrategy.MISSING)
          .doOnNext(e -> latest.put(keyFunction.apply(e), e))
          .share()
          .onBackpressureLatest()
          .observeOn(Schedulers.computation());
      this.flowable.subscribe(eventBus::post);
    }

    void removeFromCache(U key) {
      latest.remove(key);
    }

    Flowable<T> get(@SuppressWarnings("unchecked") U... keys) {
      List<U> asList = Arrays.asList(keys);
      return flowable
        .startWith(Flowable.defer(() -> Flowable.fromIterable(latest.values())))
        .filter(t -> asList.contains(keyFunction.apply(t)));
    }

    void emit(T e) {
      if (emitter.get() != null)
        emitter.get().onNext(e);
    }
  }
}
