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

import static com.gruelbox.orko.marketdata.MarketDataType.BALANCE;
import static com.gruelbox.orko.marketdata.MarketDataType.ORDER;
import static com.gruelbox.orko.marketdata.MarketDataType.ORDERBOOK;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static com.gruelbox.orko.marketdata.MarketDataType.TRADES;
import static com.gruelbox.orko.marketdata.MarketDataType.USER_TRADE;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Collections.emptySet;
import static jersey.repackaged.com.google.common.base.MoreObjects.firstNonNull;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.knowm.xchange.dto.Order.OrderType.BID;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.bitfinex.common.dto.BitfinexException;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.ExchangeSecurityException;
import org.knowm.xchange.exceptions.ExchangeUnavailableException;
import org.knowm.xchange.exceptions.FrequencyLimitExceededException;
import org.knowm.xchange.exceptions.NonceException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.exceptions.RateLimitExceededException;
import org.knowm.xchange.exceptions.SystemOverloadException;
import org.knowm.xchange.service.account.AccountService;
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
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.exchange.AccountServiceFactory;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.exchange.RateController;
import com.gruelbox.orko.exchange.TradeServiceFactory;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.CheckedExceptions;
import com.gruelbox.orko.util.SafelyDispose;

import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.ProductSubscription.ProductSubscriptionBuilder;
import info.bitrich.xchangestream.core.StreamingExchange;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.disposables.Disposable;
import si.mazi.rescu.HttpStatusIOException;

/**
 * Maintains subscriptions to multiple exchanges' market data, using web sockets where it can
 * and polling where it can't, but this is abstracted away. All clients have access to reactive
 * streams of data which are persistent and recover in the event of disconnections/reconnections.
 */
@Singleton
@VisibleForTesting
public class MarketDataSubscriptionManager extends AbstractExecutionThreadService {

  private static final int MAX_TRADES = 20;
  private static final int ORDERBOOK_DEPTH = 20;
  private static final int MINUTES_BETWEEN_EXCEPTION_NOTIFICATIONS = 15;

  private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataSubscriptionManager.class);

  private final ExchangeService exchangeService;
  private final TradeServiceFactory tradeServiceFactory;
  private final AccountServiceFactory accountServiceFactory;
  private final OrkoConfiguration configuration;
  private final NotificationService notificationService;

  private final Map<String, AtomicReference<Set<MarketDataSubscription>>> nextSubscriptions;
  private final ConcurrentMap<String, Set<MarketDataSubscription>> subscriptionsPerExchange = Maps.newConcurrentMap();
  private final ConcurrentMap<String, Set<MarketDataSubscription>> pollsPerExchange = Maps.newConcurrentMap();
  private final Multimap<String, Disposable> disposablesPerExchange = HashMultimap.create();
  private final Set<MarketDataSubscription> unavailableSubscriptions = Sets.newConcurrentHashSet();

  private final CachingPersistentPublisher<TickerEvent, TickerSpec> tickersOut;
  private final CachingPersistentPublisher<OpenOrdersEvent, TickerSpec> openOrdersOut;
  private final CachingPersistentPublisher<OrderBookEvent, TickerSpec> orderbookOut;
  private final PersistentPublisher<TradeEvent> tradesOut;
  private final CachingPersistentPublisher<BalanceEvent, String> balanceOut;
  private final PersistentPublisher<OrderChangeEvent> orderStatusChangeOut;
  private final CachingPersistentPublisher<UserTradeEvent, String> userTradesOut;

  private final ConcurrentMap<TickerSpec, Instant> mostRecentTrades = Maps.newConcurrentMap();

  private final Phaser phaser = new Phaser(1);

  private LifecycleListener lifecycleListener = new LifecycleListener() {};

  @Inject
  @VisibleForTesting
  public MarketDataSubscriptionManager(ExchangeService exchangeService, OrkoConfiguration configuration, TradeServiceFactory tradeServiceFactory, AccountServiceFactory accountServiceFactory, NotificationService notificationService) {
    this.exchangeService = exchangeService;
    this.configuration = configuration;
    this.tradeServiceFactory = tradeServiceFactory;
    this.accountServiceFactory = accountServiceFactory;
    this.notificationService = notificationService;

    this.nextSubscriptions = FluentIterable.from(exchangeService.getExchanges())
        .toMap(e -> new AtomicReference<>());

    exchangeService.getExchanges().forEach(e -> {
      subscriptionsPerExchange.put(e, ImmutableSet.of());
      pollsPerExchange.put(e, ImmutableSet.of());
    });

    this.tickersOut = new CachingPersistentPublisher<>(TickerEvent::spec);
    this.openOrdersOut = new CachingPersistentPublisher<>(OpenOrdersEvent::spec);
    this.orderbookOut = new CachingPersistentPublisher<>(OrderBookEvent::spec);
    this.tradesOut = new PersistentPublisher<>();
    this.userTradesOut = new CachingPersistentPublisher<>((UserTradeEvent e) -> e.trade().getId())
        .orderInitialSnapshotBy(iterable -> Ordering.natural().onResultOf((UserTradeEvent e) -> e.trade().getTimestamp()).sortedCopy(iterable));
    this.balanceOut = new CachingPersistentPublisher<>((BalanceEvent e) -> e.exchange() + "/" + e.currency());
    this.orderStatusChangeOut = new PersistentPublisher<>();
  }


  @VisibleForTesting
  void setLifecycleListener(LifecycleListener listener) {
    this.lifecycleListener = listener;
  }


  /**
   * Updates the subscriptions for the specified exchanges on the next loop
   * tick. The delay is to avoid a large number of new subscriptions in quick
   * succession causing rate bans on exchanges. Call with an empty set to cancel
   * all subscriptions. None of the streams (e.g. {@link #getTickers()}
   * will return anything until this is called, but there is no strict order in
   * which they need to be called.
   *
   * @param subscriptions The subscriptions.
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
   * Gets the stream of subscribed tickers, starting with any cached tickers.
   *
   * @return The stream.
   */
  public Flowable<TickerEvent> getTickers() {
    return tickersOut.getAll();
  }


  /**
   * Gets the stream of subscribed open order lists.
   *
   * @return The stream.
   */
  public Flowable<OpenOrdersEvent> getOrderSnapshots() {
    return openOrdersOut.getAll();
  }


  /**
   * Gets a stream containing updates to the order book.
   *
   * @return The stream.
   */
  public Flowable<OrderBookEvent> getOrderBookSnapshots() {
    return orderbookOut.getAll();
  }


  /**
   * Gets a stream of trades.
   *
   * @return The stream.
   */
  public Flowable<TradeEvent> getTrades() {
    return tradesOut.getAll();
  }


  /**
   * Gets a stream of user trades.
   *
   * @return The stream.
   */
  public Flowable<UserTradeEvent> getUserTrades() {
    return userTradesOut.getAll();
  }


  /**
   * Gets a stream with updates to the balance.
   *
   * @return The stream.
   */
  public Flowable<BalanceEvent> getBalances() {
    return balanceOut.getAll();
  }


  /**
   * Call immediately after submitting an order to ensure the full order details appear
   * in the event stream at some point (allows for Coinbase not providing everything).
   *
   * TODO temporary until better support is arrange in xchange-stream
   */
  public void postOrder(TickerSpec spec, Order order) {
    orderStatusChangeOut.emit(OrderChangeEvent.create(spec, order, new Date()));
  }


  /**
   * Gets a stream with binance execution reports.
   *
   * @return The stream.
   */
  public Flowable<OrderChangeEvent> getOrderChanges() {
    return orderStatusChangeOut.getAll();
  }


  @Override
  protected void run() {
    Thread.currentThread().setName(MarketDataSubscriptionManager.class.getSimpleName());
    LOGGER.info("{} started", this);
    ExecutorService threadPool = Executors.newFixedThreadPool(exchangeService.getExchanges().size());
    try {
      try {
        submitExchangesAndWaitForCompletion(threadPool);
        LOGGER.info("{} stopping; all exchanges have shut down", this);
      } catch (InterruptedException e) {
        LOGGER.info("{} stopping due to interrupt", this);
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        LOGGER.error(this + " stopping due to uncaught exception", e);
      }
    } finally {
      threadPool.shutdownNow();
      updateSubscriptions(emptySet());
      LOGGER.info("{} stopped", this);
      lifecycleListener.onStopMain();
    }
  }

  private void submitExchangesAndWaitForCompletion(ExecutorService threadPool) throws InterruptedException {
    Map<String, Future<?>> futures = new HashMap<>();
    for (String exchange : exchangeService.getExchanges()) {
      futures.put(exchange, threadPool.submit(new Poller(exchange)));
    }
    for (Entry<String, Future<?>> entry : futures.entrySet()) {
      try {
        entry.getValue().get();
      } catch (ExecutionException e) {
        LOGGER.error(entry.getKey() + " failed with uncaught exception and will not restart", e);
      }
    }
  }

  @Override
  protected void triggerShutdown() {
    super.triggerShutdown();
    phaser.arriveAndDeregister();
    phaser.forceTermination();
  }

  /**
   * Handles the market data polling and subscription cycle for an exchange.
   *
   * @author Graham Crockford
   */
  private final class Poller implements Runnable {

    private final String exchangeName;
    private StreamingExchange streamingExchange;
    private AccountService accountService;
    private MarketDataService marketDataService;
    private TradeService tradeService;

    private int phase;
    private boolean subscriptionsFailed;
    private Exception lastPollException;
    private LocalDateTime lastPollErrorNotificationTime;

    private Poller(String exchangeName) {
      this.exchangeName = exchangeName;
    }

    @Override
    public void run() {
      Thread.currentThread().setName(exchangeName);
      LOGGER.info("{} starting", exchangeName);
      try {
        initialise();
        while (!phaser.isTerminated()) {

          // Before we check for the presence of polls, determine which phase
          // we are going to wait for if there's no work to do - i.e. the
          // next wakeup.
          phase = phaser.getPhase();
          if (phase == -1)
            break;

          loop();

        }
        LOGGER.info("{} shutting down due to termination", exchangeName);
      } catch (InterruptedException e) {
        LOGGER.info("{} shutting down due to interrupt", exchangeName);
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        LOGGER.error(exchangeName + " shutting down due to uncaught exception", e);
      } finally {
        lifecycleListener.onStop(exchangeName);
      }
    }

    /**
     * This may fail when the exchange is not available, so keep trying.
     * @throws InterruptedException
     */
    private void initialise() throws InterruptedException {
      while (isRunning()) {
        try {
          Exchange exchange = exchangeService.get(exchangeName);
          this.streamingExchange = exchange instanceof StreamingExchange ? (StreamingExchange) exchange : null;
          this.accountService = accountServiceFactory.getForExchange(exchangeName);
          this.marketDataService = exchange.getMarketDataService();
          this.tradeService = tradeServiceFactory.getForExchange(exchangeName);
          break;
        } catch (Exception e) {
          LOGGER.error(exchangeName + " - failing initialising. Will retry in one minute.", e);
          Thread.sleep(60000);
        }
      }
    }

    private void loop() throws InterruptedException {

      // Check if there is a queued subscription change.  If so, apply it
      doSubscriptionChanges();

      // Check if we have any polling to do. If not, go to sleep until awoken
      // by a subscription change, unless we failed to process subscriptions,
      // in which case wake ourselves up in a few seconds to try again
      Set<MarketDataSubscription> polls = activePolls();
      if (polls.isEmpty()) {
        suspend();
        return;
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
        }
      }

      if (phaser.isTerminated())
        return;

      // We'll be extending this sort of batching to more market data types...
      if (!balanceCurrencies.isEmpty()) {
        manageExchangeExceptions(
            "Balances",
            () -> fetchBalances(balanceCurrencies).forEach(b -> balanceOut.emit(BalanceEvent.create(exchangeName, b.currency(), b))),
            () -> FluentIterable.from(polls).filter(s -> s.type().equals(BALANCE))
        );
      }
    }

    private void manageExchangeExceptions(String dataDescription, CheckedExceptions.ThrowingRunnable runnable, Supplier<Iterable<MarketDataSubscription>> toUnsubscribe) throws InterruptedException {
      try {
        runnable.run();

      } catch (InterruptedException e) {
        throw e;

      } catch (NotAvailableFromExchangeException | NotYetImplementedForExchangeException e) {

        // Disable the feature since XChange doesn't provide support for it.
        LOGGER.warn("{} not available: {} ({})", dataDescription, e.getClass().getSimpleName(), exceptionMessage(e));
        Iterables.addAll(unavailableSubscriptions, toUnsubscribe.get());

      } catch (SocketTimeoutException | SocketException | ExchangeUnavailableException | SystemOverloadException | NonceException e) {

        // Managed connectivity issues.
        LOGGER.warn("Throttling {} - {} ({}) when fetching {}", exchangeName, e.getClass().getSimpleName(), exceptionMessage(e), dataDescription);
        exchangeService.rateController(exchangeName).throttle();

      } catch (HttpStatusIOException e) {

        handleHttpStatusException(dataDescription, e);

      } catch (RateLimitExceededException | FrequencyLimitExceededException e) {

        LOGGER.error("Hit rate limiting on {} when fetching {}. Backing off", exchangeName, dataDescription);
        notificationService.error("Getting rate limiting errors on " + exchangeName + ". Pausing access and will "
            + "resume at a lower rate.");
        RateController rateController = exchangeService.rateController(exchangeName);
        rateController.backoff();
        rateController.pause();

      } catch (ExchangeException e) {
        if (e.getCause() instanceof HttpStatusIOException) {
          // TODO Bitmex is inappropriately wrapping these and should be fixed
          // for consistency. In the meantime...
          handleHttpStatusException(dataDescription, (HttpStatusIOException) e.getCause());
        } else {
          handleUnknownPollException(e);
        }
      } catch (BitfinexException e) {
        handleUnknownPollException(new ExchangeException("Bitfinex exception: " + exceptionMessage(e) + " (error code=" + e.getError() + ")", e));
      } catch (Exception e) {
        handleUnknownPollException(e);
      }
    }

    private void handleHttpStatusException(String dataDescription, HttpStatusIOException e) {
      if (e.getHttpStatusCode() == 408 || e.getHttpStatusCode() == 502 || e.getHttpStatusCode() == 504 || e.getHttpStatusCode() == 521) {
        // Usually these are rejections at CloudFlare (Coinbase Pro & Kraken being common cases) or connection timeouts.
        LOGGER.warn("Throttling {} - failed at gateway ({} - {}) when fetching {}", exchangeName, e.getHttpStatusCode(), exceptionMessage(e), dataDescription);
        exchangeService.rateController(exchangeName).throttle();
      } else {
        handleUnknownPollException(e);
      }
    }

    private String exceptionMessage(Throwable e) {
      if (e.getMessage() == null) {
        if (e.getCause() == null) {
          return "No description";
        } else {
          return exceptionMessage(e.getCause());
        }
      } else {
        return e.getMessage();
      }
    }

    private void handleUnknownPollException(Exception e) {
      LocalDateTime now = now();
      String exceptionMessage = exceptionMessage(e);
      if (lastPollException == null ||
          !lastPollException.getClass().equals(e.getClass()) ||
          !firstNonNull(exceptionMessage(lastPollException), "").equals(exceptionMessage) ||
          lastPollErrorNotificationTime.until(now, MINUTES) > MINUTES_BETWEEN_EXCEPTION_NOTIFICATIONS) {
        lastPollErrorNotificationTime = now;
        LOGGER.error("Error fetching data for " + exchangeName, e);
        notificationService.error("Throttling access to " + exchangeName + " due to server error (" + e.getClass().getSimpleName() + " - " + exceptionMessage + ")");
      } else {
        LOGGER.error("Repeated error fetching data for {} ({})", exchangeName, exceptionMessage);
      }
      lastPollException = e;
      exchangeService.rateController(exchangeName).throttle();
    }

    /**
     * Actually performs the subscription changes. Occurs synchronously in the
     * poll loop.
     */
    private void doSubscriptionChanges() {
      LOGGER.debug("{} - start subscription check", exchangeName);
      subscriptionsFailed = false;

      // Pull the subscription change off the queue. If there isn't one,
      // we're done
      Set<MarketDataSubscription> subscriptions = nextSubscriptions.get(exchangeName).getAndSet(null);
      if (subscriptions == null)
        return;

      try {

        // Get the current subscriptions
        Set<MarketDataSubscription> oldSubscriptions = FluentIterable.from(Iterables.concat(
            subscriptionsPerExchange.get(exchangeName),
            pollsPerExchange.get(exchangeName)
          ))
          .toSet();

        // If there's no difference, we're good, done
        if (subscriptions.equals(oldSubscriptions)) {
          return;
        }

        // Otherwise, let's crack on
        LOGGER.info("{} - updating subscriptions to: {} from {}", exchangeName, subscriptions, oldSubscriptions);

        // Disconnect any streaming exchanges where the tickers currently
        // subscribed mismatch the ones we want.
        if (!oldSubscriptions.isEmpty()) {
          disconnect();
        }

        // Clear cached tickers and order books for anything we've unsubscribed so that we don't feed out-of-date data
        Sets.difference(oldSubscriptions, subscriptions)
          .forEach(this::clearCacheForSubscription);

        // Add new subscriptions if we have any
        if (subscriptions.isEmpty()) {
          pollsPerExchange.put(exchangeName, ImmutableSet.of());
          LOGGER.debug("{} - polls cleared", exchangeName);
        } else {
          subscribe(subscriptions);
        }
      } catch (Exception e) {
        subscriptionsFailed = true;
        LOGGER.error("Error updating subscriptions", e);
        if (nextSubscriptions.get(exchangeName).compareAndSet(null, subscriptions)) {
          int arrivedPhase = phaser.arrive();
          LOGGER.debug("Progressing to phase {}", arrivedPhase);
        }
        throw e;
      }
    }

    private void clearCacheForSubscription(MarketDataSubscription subscription) {
      tickersOut.removeFromCache(subscription.spec());
      orderbookOut.removeFromCache(subscription.spec());
      openOrdersOut.removeFromCache(subscription.spec());
      userTradesOut.removeFromCache(t -> t.spec().equals(subscription.spec()));
      balanceOut.removeFromCache(subscription.spec().exchange() + "/" + subscription.spec().base());
      balanceOut.removeFromCache(subscription.spec().exchange() + "/" + subscription.spec().counter());
    }

    private ImmutableSet<MarketDataSubscription> activePolls() {
      return FluentIterable.from(pollsPerExchange.get(exchangeName))
          .filter(s -> !unavailableSubscriptions.contains(s)).toSet();
    }

    private void disconnect() {
      if (streamingExchange != null) {
        SafelyDispose.of(disposablesPerExchange.removeAll(exchangeName));
        try {
          streamingExchange.disconnect().blockingAwait();
        } catch (Exception e) {
          LOGGER.error("Error disconnecting from " + exchangeName, e);
        }
      } else {
        Iterator<Entry<TickerSpec, Instant>> iterator = mostRecentTrades.entrySet().iterator();
        while (iterator.hasNext()) {
          if (iterator.next().getKey().exchange().equals(exchangeName))
            iterator.remove();
        }
      }
    }

    private void subscribe(Set<MarketDataSubscription> subscriptions) {

      Builder<MarketDataSubscription> pollingBuilder = ImmutableSet.builder();

      if (streamingExchange != null) {
        Set<MarketDataSubscription> remainingSubscriptions = openSubscriptionsWherePossible(subscriptions);
        pollingBuilder.addAll(remainingSubscriptions);
      } else {
        pollingBuilder.addAll(subscriptions);
      }

      Set<MarketDataSubscription> polls = pollingBuilder.build();
      pollsPerExchange.put(exchangeName, pollingBuilder.build());
      LOGGER.debug("{} - polls now set to: {}", exchangeName, polls);
    }


    private Set<MarketDataSubscription> openSubscriptionsWherePossible(Set<MarketDataSubscription> subscriptions) {

      connectExchange(subscriptions);

      HashSet<MarketDataSubscription> connected = new HashSet<>(subscriptions);
      ImmutableSet.Builder<MarketDataSubscription> remainder = ImmutableSet.builder();
      List<Disposable> disposables = new ArrayList<>();

      Consumer<MarketDataSubscription> markAsNotSubscribed = s -> {
        remainder.add(s);
        connected.remove(s);
      };

      Set<String> balanceCurrencies = new HashSet<>();
      for (MarketDataSubscription s : subscriptions) {

        // User trade and balance subscriptions, for now, we will poll even if we are
        // already getting them from the socket. This will persist until we can
        // safely detect and correct ordering/missed messages on the socket streams.
        if (s.type().equals(USER_TRADE) || s.type().equals(BALANCE)) {
          remainder.add(s);
        }

        if (s.type().equals(BALANCE)) {
          // Aggregate the currencies and do these next
          balanceCurrencies.add(s.spec().base());
          balanceCurrencies.add(s.spec().counter());
        } else {
          try {
            disposables.add(connectSubscription(s));
          } catch (NotAvailableFromExchangeException e) {
            markAsNotSubscribed.accept(s);
          } catch (ExchangeSecurityException | NotYetImplementedForExchangeException e) {
            LOGGER.debug("Not subscribing to {} on socket due to {}: {}", s.key(), e.getClass().getSimpleName(), e.getMessage());
            markAsNotSubscribed.accept(s);
          }
        }
      }

      try {
        for (String currency : balanceCurrencies) {
          disposables.add(
            streamingExchange.getStreamingAccountService().getBalanceChanges(Currency.getInstance(currency), "exchange") // TODO bitfinex walletId. Should manage multiple wallets properly
              .map(Balance::create)
              .map(b -> BalanceEvent.create(exchangeName, b.currency(), b)) // TODO consider timestamping?
              .subscribe(balanceOut::emit, e -> LOGGER.error("Error in balance stream for " + exchangeName + "/" + currency, e)));
        }
      } catch (NotAvailableFromExchangeException e) {
        subscriptions.stream()
          .filter(s -> s.type().equals(BALANCE))
          .forEach(markAsNotSubscribed);
      } catch (ExchangeSecurityException | NotYetImplementedForExchangeException e) {
        LOGGER.debug("Not subscribing to {}/{} on socket due to {}: {}", exchangeName, "Balances", e.getClass().getSimpleName(), e.getMessage());
        subscriptions.stream()
          .filter(s -> s.type().equals(BALANCE))
          .forEach(markAsNotSubscribed);
      }

      subscriptionsPerExchange.put(exchangeName, Collections.unmodifiableSet(connected));
      disposablesPerExchange.putAll(exchangeName, disposables);
      return remainder.build();
    }

    private Disposable connectSubscription(MarketDataSubscription sub) {
      switch (sub.type()) {
        case ORDERBOOK:
          return streamingExchange.getStreamingMarketDataService().getOrderBook(sub.spec().currencyPair())
              .map(t -> OrderBookEvent.create(sub.spec(), t))
              .subscribe(orderbookOut::emit, e -> LOGGER.error("Error in order book stream for " + sub, e));
        case TICKER:
          LOGGER.debug("Subscribing to {}", sub.spec());
          return streamingExchange.getStreamingMarketDataService().getTicker(sub.spec().currencyPair())
              .map(t -> TickerEvent.create(sub.spec(), t))
              .subscribe(tickersOut::emit, e -> LOGGER.error("Error in ticker stream for " + sub, e));
        case TRADES:
          return streamingExchange.getStreamingMarketDataService().getTrades(sub.spec().currencyPair())
              .map(t -> convertBinanceOrderType(sub, t))
              .map(t -> TradeEvent.create(sub.spec(), t))
              .subscribe(tradesOut::emit, e -> LOGGER.error("Error in trade stream for " + sub, e));
        case USER_TRADE:
          return streamingExchange.getStreamingTradeService().getUserTrades(sub.spec().currencyPair())
              .map(t -> UserTradeEvent.create(sub.spec(), t))
              .subscribe(userTradesOut::emit, e -> LOGGER.error("Error in trade stream for " + sub, e));
        case ORDER:
          return streamingExchange.getStreamingTradeService().getOrderChanges(sub.spec().currencyPair())
              .map(t -> OrderChangeEvent.create(sub.spec(), t, new Date())) // TODO need server side timestamping
              .subscribe(orderStatusChangeOut::emit, e -> LOGGER.error("Error in order stream for " + sub, e));
        default:
          throw new NotAvailableFromExchangeException();
      }
    }


    /**
     * TODO Temporary fix for https://github.com/knowm/XChange/issues/2468#issuecomment-441440035
     */
    private Trade convertBinanceOrderType(MarketDataSubscription sub, Trade t) {
      if (sub.spec().exchange().equals(Exchanges.BINANCE)) {
        return Trade.Builder.from(t).type(t.getType() == BID ? ASK : BID).build();
      } else {
        return t;
      }
    }

    private void connectExchange(Collection<MarketDataSubscription> subscriptionsForExchange) {
      if (subscriptionsForExchange.isEmpty())
        return;
      LOGGER.info("Connecting to exchange: {}", exchangeName);
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
          if (s.type().equals(USER_TRADE)) {
            builder.addUserTrades(s.spec().currencyPair());
          }
          if (s.type().equals(ORDER)) {
            builder.addOrders(s.spec().currencyPair());
          }
          if (s.type().equals(BALANCE)) {
            builder.addBalances(s.spec().currencyPair().base);
            builder.addBalances(s.spec().currencyPair().counter);
          }
        });
      exchangeService.rateController(exchangeName).acquire();
      streamingExchange.connect(builder.build()).blockingAwait();
      LOGGER.info("Connected to exchange: {}", exchangeName);
    }

    private void suspend() throws InterruptedException {
      LOGGER.debug("{} - poll going to sleep", exchangeName);
      try {
        if (subscriptionsFailed) {
          long defaultSleep = (long) configuration.getLoopSeconds() * 1000;
          phaser.awaitAdvanceInterruptibly(phase, defaultSleep, TimeUnit.MILLISECONDS);
        } else {
          LOGGER.debug("{} - sleeping until phase {}", exchangeName, phase);
          lifecycleListener.onBlocked(exchangeName);
          phaser.awaitAdvanceInterruptibly(phase);
          LOGGER.debug("{} - poll woken up on request", exchangeName);
        }
      } catch (TimeoutException e) {
        // fine
      } catch (InterruptedException e) {
        throw e;
      } catch (Exception e) {
        LOGGER.error("Failure in phaser wait for " + exchangeName, e);
      }
    }

    private Iterable<Balance> fetchBalances(Collection<String> currencyCodes) throws IOException, InterruptedException {
      Map<String, Balance> result = new HashMap<>();
      currencyCodes.stream().map(Balance::zero)
        .forEach(balance -> result.put(balance.currency(), balance));
      wallet().getBalances().entrySet().stream()
        .map(Map.Entry::getValue)
        .filter(balance -> currencyCodes.contains(balance.getCurrency().getCurrencyCode()))
        .map(Balance::create)
        .forEach(balance -> result.put(balance.currency(), balance));
      return result.values();
    }

    private Wallet wallet() throws IOException {
      exchangeService.rateController(exchangeName).acquire();
      Wallet wallet;
      if (exchangeName.equals(Exchanges.BITFINEX)) {
        wallet = accountService.getAccountInfo().getWallet("exchange");
      } else if (exchangeName.equals(Exchanges.KUCOIN)) {
        wallet = accountService.getAccountInfo().getWallet("trade");
        if (wallet == null)
          wallet = accountService.getAccountInfo().getWallet();
      } else {
        wallet = accountService.getAccountInfo().getWallet();
      }
      if (wallet == null) {
        throw new IllegalStateException("No wallet returned");
      }
      return wallet;
    }

    private void fetchAndBroadcast(MarketDataSubscription subscription) throws InterruptedException {
      exchangeService.rateController(exchangeName).acquire();
      TickerSpec spec = subscription.spec();
      manageExchangeExceptions(
          subscription.key(),
          () -> {
            switch (subscription.type()) {
              case TICKER:
                pollAndEmitTicker(spec);
                break;
              case ORDERBOOK:
                pollAndEmitOrderbook(spec);
                break;
              case TRADES:
                pollAndEmitTrades(subscription);
                break;
              case OPEN_ORDERS:
                pollAndEmitOpenOrders(subscription);
                break;
              case USER_TRADE:
                pollAndEmitUserTradeHistory(subscription);
                break;
              case ORDER:
                // Not currently supported by polling
                break;
              default:
                throw new IllegalStateException("Market data type " + subscription.type() + " not supported in this way");
            }
          },
          () -> ImmutableList.of(subscription)
      );
    }

    private void pollAndEmitUserTradeHistory(MarketDataSubscription subscription) throws IOException {
      TradeHistoryParams tradeHistoryParams = tradeHistoryParams(subscription);
      tradeService.getTradeHistory(tradeHistoryParams)
        .getUserTrades()
        .forEach(trade -> userTradesOut.emit(UserTradeEvent.create(subscription.spec(), trade)));
    }

    @SuppressWarnings("unchecked")
    private void pollAndEmitOpenOrders(MarketDataSubscription subscription) throws IOException {
      OpenOrdersParams openOrdersParams = openOrdersParams(subscription);

      Date originatingTimestamp = new Date();
      OpenOrders fetched = tradeService.getOpenOrders(openOrdersParams);

      // TODO GDAX PR required
      if (subscription.spec().exchange().equals(Exchanges.GDAX)) {
        ImmutableList<LimitOrder> filteredOpen = FluentIterable.from(fetched.getOpenOrders()).filter(openOrdersParams::accept).toList();
        ImmutableList<? extends Order> filteredHidden = FluentIterable.from(fetched.getHiddenOrders()).toList();
        fetched = new OpenOrders(filteredOpen, (List<Order>) filteredHidden);
      }

      openOrdersOut.emit(OpenOrdersEvent.create(subscription.spec(), fetched, originatingTimestamp));
    }

    private void pollAndEmitTrades(MarketDataSubscription subscription) throws IOException {
      marketDataService.getTrades(subscription.spec().currencyPair())
        .getTrades()
        .stream()
        .forEach(t ->
          mostRecentTrades.compute(subscription.spec(), (k, previousTiming) -> {
            Instant thisTradeTiming = t.getTimestamp().toInstant();
            Instant newMostRecent = previousTiming;
            if (previousTiming == null) {
              newMostRecent = thisTradeTiming;
            } else if (thisTradeTiming.isAfter(previousTiming)) {
              newMostRecent = thisTradeTiming;
              tradesOut.emit(TradeEvent.create(subscription.spec(), t));
            }
            return newMostRecent;
          })
        );
    }

    private void pollAndEmitOrderbook(TickerSpec spec) throws IOException {
      OrderBook orderBook = marketDataService.getOrderBook(spec.currencyPair(), exchangeOrderbookArgs(spec));
      orderbookOut.emit(OrderBookEvent.create(spec, orderBook));
    }

    private Object[] exchangeOrderbookArgs(TickerSpec spec) {
      if (spec.exchange().equals(Exchanges.BITMEX)) {
        return new Object[] { };
      } else {
        return new Object[] { ORDERBOOK_DEPTH, ORDERBOOK_DEPTH };
      }
    }

    private void pollAndEmitTicker(TickerSpec spec) throws IOException {
      tickersOut.emit(TickerEvent.create(spec, marketDataService.getTicker(spec.currencyPair())));
    }

    private TradeHistoryParams tradeHistoryParams(MarketDataSubscription subscription) {
      TradeHistoryParams params;

      // TODO fix with pull requests
      if (subscription.spec().exchange().equals(Exchanges.BITMEX) || subscription.spec().exchange().equals(Exchanges.GDAX)) {
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

    private OpenOrdersParams openOrdersParams(MarketDataSubscription subscription) {
      OpenOrdersParams params = null;
      try {
        params = tradeService.createOpenOrdersParams();
      } catch (NotYetImplementedForExchangeException e) {
        // Fiiiiine Bitmex
      }
      if (params == null) {
        // Bitfinex & Bitmex
        params = new DefaultOpenOrdersParamCurrencyPair(subscription.spec().currencyPair());
      } else if (params instanceof OpenOrdersParamCurrencyPair) {
        ((OpenOrdersParamCurrencyPair) params).setCurrencyPair(subscription.spec().currencyPair());
      } else {
        throw new UnsupportedOperationException("Don't know how to read open orders on this exchange: " + subscription.spec().exchange());
      }
      return params;
    }

  }

  private class PersistentPublisher<T> {
    private final Flowable<T> flowable;
    private final AtomicReference<FlowableEmitter<T>> emitter = new AtomicReference<>();

    PersistentPublisher() {
      this.flowable = setup(Flowable.create((FlowableEmitter<T> e) -> emitter.set(e.serialize()), BackpressureStrategy.MISSING))
          .share()
          .onBackpressureLatest();
    }

    Flowable<T> setup(Flowable<T> base) {
      return base;
    }

    Flowable<T> getAll() {
      return flowable;
    }

    final void emit(T e) {
      if (emitter.get() != null)
        emitter.get().onNext(e);
    }
  }

  private final class CachingPersistentPublisher<T, U> extends PersistentPublisher<T> {
    private final ConcurrentMap<U, T> latest = Maps.newConcurrentMap();
    private final Function<T, U> keyFunction;
    private Function<Iterable<T>, Iterable<T>> initialSnapshotSortFunction;

    CachingPersistentPublisher(Function<T, U> keyFunction) {
      super();
      this.keyFunction = keyFunction;
    }

    @Override
    Flowable<T> setup(Flowable<T> base) {
      return base.doOnNext(e -> latest.put(this.keyFunction.apply(e), e));
    }

    void removeFromCache(U key) {
      latest.remove(key);
    }

    void removeFromCache(Predicate<T> matcher) {
      Set<U> removals = new HashSet<>();
      latest.entrySet().stream()
        .filter(e -> matcher.test(e.getValue()))
        .map(Entry::getKey)
        .forEach(removals::add);
      removals.forEach(latest::remove);
    }

    public CachingPersistentPublisher<T, U> orderInitialSnapshotBy(UnaryOperator<Iterable<T>> ordering) {
      this.initialSnapshotSortFunction = ordering;
      return this;
    }

    @Override
    Flowable<T> getAll() {
      if (initialSnapshotSortFunction == null) {
        return super.getAll().startWith(Flowable.defer(() -> Flowable.fromIterable(latest.values())));
      } else {
        return super.getAll().startWith(Flowable.defer(() -> Flowable.fromIterable(initialSnapshotSortFunction.apply(latest.values()))));
      }
    }
  }

  /**
   * For testing. Fires signals at key events allowing tests to orchestrate.
   */
  interface LifecycleListener {
    default void onBlocked(String exchange) {}
    default void onStop(String exchange) {}
    default void onStopMain() {}
  }
}
