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
import static com.gruelbox.orko.marketdata.MarketDataType.ORDERBOOK;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static com.gruelbox.orko.marketdata.MarketDataType.TRADES;
import static java.util.Collections.emptySet;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.knowm.xchange.dto.Order.OrderType.BID;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.bitmex.BitmexPrompt;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
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
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.exchange.AccountServiceFactory;
import com.gruelbox.orko.exchange.ExchangeConfiguration;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.exchange.TradeServiceFactory;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.SafelyDispose;

import info.bitrich.xchangestream.binance.BinanceStreamingMarketDataService;
import info.bitrich.xchangestream.binance.dto.ExecutionReportBinanceUserTransaction;
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
 * streams of data which are persistent and recover in the event of disconnections/reconnections.
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
  private final OrkoConfiguration configuration;
  private final EventBus eventBus;

  private final Map<String, AtomicReference<Set<MarketDataSubscription>>> nextSubscriptions;
  private final ConcurrentMap<String, Set<MarketDataSubscription>> subscriptionsPerExchange = Maps.newConcurrentMap();
  private final ConcurrentMap<String, Set<MarketDataSubscription>> pollsPerExchange = Maps.newConcurrentMap();
  private final Multimap<String, Disposable> disposablesPerExchange = HashMultimap.create();
  private final Set<MarketDataSubscription> unavailableSubscriptions = Sets.newConcurrentHashSet();

  private final CachingPersistentPublisher<TickerEvent, TickerSpec> tickersOut;
  private final CachingPersistentPublisher<OpenOrdersEvent, TickerSpec> openOrdersOut;
  private final CachingPersistentPublisher<OrderBookEvent, TickerSpec> orderbookOut;
  private final PersistentPublisher<TradeEvent> tradesOut;
  private final CachingPersistentPublisher<TradeHistoryEvent, TickerSpec> userTradeHistoryOut;
  private final CachingPersistentPublisher<BalanceEvent, String> balanceOut;
  private final PersistentPublisher<ExecutionReportBinanceUserTransaction> binanceExecutionReportsOut;

  private final ConcurrentMap<TickerSpec, Instant> mostRecentTrades = Maps.newConcurrentMap();

  private final Phaser phaser  = new Phaser(1);


  @Inject
  @VisibleForTesting
  public MarketDataSubscriptionManager(ExchangeService exchangeService, OrkoConfiguration configuration, TradeServiceFactory tradeServiceFactory, AccountServiceFactory accountServiceFactory, EventBus eventBus) {
    this.exchangeService = exchangeService;
    this.configuration = configuration;
    this.tradeServiceFactory = tradeServiceFactory;
    this.accountServiceFactory = accountServiceFactory;
    this.eventBus = eventBus;

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
    this.userTradeHistoryOut = new CachingPersistentPublisher<>(TradeHistoryEvent::spec);
    this.balanceOut = new CachingPersistentPublisher<>((BalanceEvent e) -> e.exchange() + "/" + e.currency());
    this.binanceExecutionReportsOut = new PersistentPublisher<>();
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
  public Flowable<OpenOrdersEvent> getOpenOrders() {
    return openOrdersOut.getAll();
  }


  /**
   * Gets a stream containing updates to the order book.
   *
   * @return The stream.
   */
  public Flowable<OrderBookEvent> getOrderBooks() {
    return orderbookOut.getAll();
  }


  /**
   * Gets a stream of trades.
   *
   * @return The stream.
   */
  public Flowable<TradeEvent> getTrades() {
    return tradesOut.getAll().filter(t -> !UserTrade.class.isInstance(t));
  }


  /**
   * Gets a stream with updates to the recent trade history.
   *
   *  @return The stream.
   */
  public Flowable<TradeHistoryEvent> getUserTradeHistory() {
    return userTradeHistoryOut.getAll();
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
   * Gets a stream with binance execution reports.
   *
   * @return The stream.
   */
  public Flowable<ExecutionReportBinanceUserTransaction> getBinanceExecutionReports() {
    return binanceExecutionReportsOut.getAll();
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
            tickersOut.removeFromCache(s.spec());
            orderbookOut.removeFromCache(s.spec());
            openOrdersOut.removeFromCache(s.spec());
            userTradeHistoryOut.removeFromCache(s.spec());
            balanceOut.removeFromCache(s.spec().exchange() + "/" + s.spec().base());
            balanceOut.removeFromCache(s.spec().exchange() + "/" + s.spec().counter());
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
      try {
        ((StreamingExchange) exchange).disconnect().blockingAwait();
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
                .subscribe(orderbookOut::emit, e -> LOGGER.error("Error in order book stream for " + sub, e));
          case TICKER:
            LOGGER.debug("Subscribing to {}", sub.spec());
            return streaming.getTicker(sub.spec().currencyPair())
                .map(t -> TickerEvent.create(sub.spec(), t))
                .subscribe(tickersOut::emit, e -> LOGGER.error("Error in ticker stream for " + sub, e));
          case TRADES:
            return streaming.getTrades(sub.spec().currencyPair())
                .map(t -> convertBinanceOrderType(sub, t))
                .map(t -> TradeEvent.create(sub.spec(), t))
                .subscribe(tradesOut::emit, e -> LOGGER.error("Error in trade stream for " + sub, e));
          default:
            throw new IllegalStateException("Unexpected market data type: " + sub.type());
        }
      })
    );

    if (Exchanges.BINANCE.equals(exchangeName) && hasBinanceApiKey()) {
      BinanceStreamingMarketDataService binance = (BinanceStreamingMarketDataService) streaming;
      disposablesPerExchange.put(
        exchangeName,
        binance.getRawExecutionReports()
          .subscribe(
            binanceExecutionReportsOut::emit,
            e -> LOGGER.error("Error in binance execution report stream", e)
          )
      );
    }
  }


  private boolean hasBinanceApiKey() {
    if (configuration.getExchanges() == null)
      return false;
    ExchangeConfiguration binanceConfiguration = configuration.getExchanges().get(Exchanges.BINANCE);
    if (binanceConfiguration == null)
      return false;
    return !StringUtils.isEmpty(binanceConfiguration.getApiKey());
  }

  /**
   * TODO Temporary fix for https://github.com/knowm/XChange/issues/2468#issuecomment-441440035
   */
  private Trade convertBinanceOrderType(MarketDataSubscription sub, Trade t) {
    if (sub.spec().exchange().equals(Exchanges.BINANCE)) {
      return new Trade(
        t.getType() == BID ? ASK : BID,
        t.getOriginalAmount(),
        t.getCurrencyPair(),
        t.getPrice(),
        t.getTimestamp(),
        t.getId()
      );
    } else {
      return t;
    }
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
    this.tickersOut.dispose();
    this.openOrdersOut.dispose();
    this.orderbookOut .dispose();
    this.tradesOut.dispose();
    this.userTradeHistoryOut.dispose();
    this.balanceOut.dispose();
    this.binanceExecutionReportsOut.dispose();
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
    try {
      while (!phaser.isTerminated()) {

        // Before we check for the presence of polls, determine which phase
        // we are going to wait for if there's no work to do - i.e. the
        // next wakeup.
        int phase = phaser.getPhase();
        if (phase == -1)
          break;

        // Handle any pending resubscriptions. Pause after a resubscription
        // since it probably counts as an API call and thus toward the
        // rate limit
        LOGGER.debug("{} - start subscription check", exchangeName);
        boolean subscriptionsFailed = false;
        try {
          if (doSubscriptionChanges(exchangeName)) {
            sleep(exchangeName);
          }
        } catch (InterruptedException e) {
          throw e;
        } catch (Exception e) {
          subscriptionsFailed = true;
        }

        // Check if we have any polling to do. If not, go to sleep until awoken
        // by a subscription change, unless we failed to process subscriptions,
        // in which case wake ourselves up in a few seconds to try again
        Set<MarketDataSubscription> polls = activePollsForExchange(exchangeName);
        if (polls.isEmpty()) {
          suspendPollThread(exchangeName, phase, subscriptionsFailed);
          continue;
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
            sleep(exchangeName);
          }
        }

        if (phaser.isTerminated())
          break;

        // We'll be extending this sort of batching to more market data types...
        if (!balanceCurrencies.isEmpty()) {
          try {
            fetchBalances(exchangeName, balanceCurrencies)
              .forEach(b -> balanceOut.emit(BalanceEvent.create(exchangeName, b.currency(), b)));
          } catch (NotAvailableFromExchangeException e) {
            LOGGER.warn("{} not available on {}" , BALANCE, exchangeName);
            Iterables.addAll(unavailableSubscriptions, FluentIterable.from(polls).filter(s -> s.type().equals(BALANCE)));
          } catch (Exception e) {
            if (Exchanges.KUCOIN.equals(exchangeName)) {
              // Kucoin does this literally all the time, so to save our logs, just write the message
              LOGGER.error("Error fetching balance on " + exchangeName + " (" + e.getMessage() + ")");
            } else {
              LOGGER.error("Error fetching balance on " + exchangeName, e);
            }
          }
          if (phaser.isTerminated())
            break;
          sleep(exchangeName);
        }

      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }


  private void suspendPollThread(String exchangeName, int phase, boolean subscriptionsFailed) {
    LOGGER.debug("{} - poll going to sleep", exchangeName);
    try {
      if (subscriptionsFailed) {
        long defaultSleep = (long) configuration.getLoopSeconds() * 1000;
        phaser.awaitAdvanceInterruptibly(phase, defaultSleep, TimeUnit.MILLISECONDS);
      } else {
        LOGGER.debug("{} - sleeping until phase {}", exchangeName, phase);
        phaser.awaitAdvanceInterruptibly(phase);
        LOGGER.debug("{} - poll woken up on request", exchangeName);
      }
    } catch (TimeoutException e) {
      // fine
    } catch (Exception e) {
      LOGGER.error("Failure in phaser wait for " + exchangeName, e);
    }
  }


  private ImmutableSet<MarketDataSubscription> activePollsForExchange(String exchangeName) {
    return FluentIterable.from(pollsPerExchange.get(exchangeName))
        .filter(s -> !unavailableSubscriptions.contains(s)).toSet();
  }

  private void sleep(String exchangeName) throws InterruptedException {
    LOGGER.debug("{} pausing between API calls", exchangeName);
    Thread.sleep(exchangeService.safePollDelay(exchangeName));
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
      TickerSpec spec = subscription.spec();
      MarketDataService marketDataService = exchangeService.get(spec.exchange()).getMarketDataService();
      switch (subscription.type()) {
        case TICKER:
          pollAndEmitTicker(spec, marketDataService);
          break;
        case ORDERBOOK:
          pollAndEmitOrderbook(spec, marketDataService);
          break;
        case TRADES:
          pollAndEmitTrades(subscription, marketDataService);
          break;
        case OPEN_ORDERS:
          pollAndEmitOpenOrders(subscription, spec);
          break;
        case USER_TRADE_HISTORY:
          pollAndEmitUserTradeHistory(subscription, spec);
          break;
        default:
          throw new IllegalStateException("Market data type " + subscription.type() + " not supported in this way");
      }
    } catch (NotAvailableFromExchangeException e) {
      LOGGER.warn(subscription.type() + " not available on " + subscription.spec().exchange());
      unavailableSubscriptions.add(subscription);
    } catch (Exception e) {
      if (Exchanges.KUCOIN.equals(subscription.spec().exchange())) {
        // Kucoin does this literally all the time, so to save our logs, just write the message
        LOGGER.error("Error fetching market data: " + subscription + " (" + e.getMessage() + ")");
      } else {
        LOGGER.error("Error fetching market data: " + subscription, e);
      }
    }
  }

  private void pollAndEmitUserTradeHistory(MarketDataSubscription subscription, TickerSpec spec) throws IOException {
    TradeService tradeService = tradeServiceFactory.getForExchange(subscription.spec().exchange());
    TradeHistoryParams tradeHistoryParams = tradeHistoryParams(subscription, tradeService);
    ImmutableList<UserTrade> trades = ImmutableList.copyOf(tradeService.getTradeHistory(tradeHistoryParams).getUserTrades());
    userTradeHistoryOut.emit(TradeHistoryEvent.create(spec, trades));
  }

  @SuppressWarnings("unchecked")
  private void pollAndEmitOpenOrders(MarketDataSubscription subscription, TickerSpec spec) throws IOException {
    TradeService tradeService = tradeServiceFactory.getForExchange(subscription.spec().exchange());
    OpenOrdersParams openOrdersParams = openOrdersParams(subscription, tradeService);
    OpenOrders fetched = tradeService.getOpenOrders(openOrdersParams);

    // TODO GDAX PR required
    if (subscription.spec().exchange().equals(Exchanges.GDAX) || subscription.spec().exchange().equals(Exchanges.GDAX_SANDBOX)) {
      ImmutableList<LimitOrder> filteredOpen = FluentIterable.from(fetched.getOpenOrders()).filter(openOrdersParams::accept).toList();
      ImmutableList<? extends Order> filteredHidden = FluentIterable.from(fetched.getHiddenOrders()).toList();
      fetched = new OpenOrders(filteredOpen, (List<Order>) filteredHidden);
    }

    openOrdersOut.emit(OpenOrdersEvent.create(spec, fetched));
  }

  private void pollAndEmitTrades(MarketDataSubscription subscription, MarketDataService marketDataService) throws IOException {
    marketDataService.getTrades(exchangePair(subscription.spec()), exchangeTradesArgs(subscription.spec()))
      .getTrades()
      .stream()
      .forEach(t -> {
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
        });
      });
  }

  private Object[] exchangeTradesArgs(TickerSpec spec) {
    return spec.exchange().equals(Exchanges.BITMEX)
        ? bitmexArgs(spec)
        : new Object[] {};
  }

  private Object[] bitmexArgs(TickerSpec spec) {
    // TODO need to think about how to manage differnet Bitmex contracts,
    // in the meantime just assume we'll use perps if we can and monthly
    // otherwise
    return new Object[] {
        (spec.base().equals("XBT") || spec.base().equals("ETH"))
          ? BitmexPrompt.PERPETUAL
          : BitmexPrompt.QUARTERLY
    };
  }

  private CurrencyPair bitmexCurrencyPair(TickerSpec spec) {
    // TODO need solution for https://github.com/knowm/XChange/issues/2886
    if (spec.counter().equals("Z18"))
      return new CurrencyPair(spec.base(), "BTC");
    else
      return spec.currencyPair();
  }

  private CurrencyPair exchangePair(TickerSpec spec) {
    return spec.exchange().equals(Exchanges.BITMEX)
        ? bitmexCurrencyPair(spec)
        : spec.currencyPair();
  }

  private void pollAndEmitOrderbook(TickerSpec spec, MarketDataService marketDataService) throws IOException {
    OrderBook orderBook = marketDataService.getOrderBook(exchangePair(spec), exchangeOrderbookArgs(spec));

    // TODO pending https://github.com/knowm/XChange/pull/2887
    if (Exchanges.BITMEX.equals(spec.exchange())) {
      Collections.sort(orderBook.getAsks(), Ordering.natural().onResultOf(LimitOrder::getLimitPrice));
    }

    orderbookOut.emit(OrderBookEvent.create(spec, orderBook));
  }

  private Object[] exchangeOrderbookArgs(TickerSpec spec) {
    if (spec.exchange().equals(Exchanges.BITMEX)) {
      return bitmexArgs(spec);
    } else if (spec.exchange().equals(Exchanges.CRYPTOPIA)) {
      // TODO submit a PR to xChange for this
      long longValue = ORDERBOOK_DEPTH;
      return new Object[] { longValue, longValue };
    } else {
      return new Object[] { ORDERBOOK_DEPTH, ORDERBOOK_DEPTH };
    }
  }

  private void pollAndEmitTicker(TickerSpec spec, MarketDataService marketDataService) throws IOException {
    tickersOut.emit(TickerEvent.create(spec, marketDataService.getTicker(spec.currencyPair())));
  }

  private TradeHistoryParams tradeHistoryParams(MarketDataSubscription subscription, TradeService tradeService) {
    TradeHistoryParams params;

    // TODO fix with pull request
    if (subscription.spec().exchange().equals(Exchanges.GDAX) || subscription.spec().exchange().equals(Exchanges.GDAX_SANDBOX)) {
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

  private class PersistentPublisher<T> implements Disposable {
    private final Flowable<T> flowable;
    private final AtomicReference<FlowableEmitter<T>> emitter = new AtomicReference<>();
    private final Disposable subscription;

    PersistentPublisher() {
      this.flowable = setup(Flowable.create((FlowableEmitter<T> e) -> emitter.set(e.serialize()), BackpressureStrategy.MISSING))
          .share()
          .onBackpressureLatest()
          .observeOn(Schedulers.computation());
      subscription = this.flowable.subscribe(eventBus::post);
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

    @Override
    public void dispose() {
      subscription.dispose();
    }

    @Override
    public boolean isDisposed() {
      return subscription.isDisposed();
    }
  }

  private final class CachingPersistentPublisher<T, U> extends PersistentPublisher<T> {
    private final ConcurrentMap<U, T> latest = Maps.newConcurrentMap();
    private final Function<T, U> keyFunction;

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

    @Override
    Flowable<T> getAll() {
      return super.getAll().startWith(Flowable.defer(() -> Flowable.fromIterable(latest.values())));
    }
  }
}
