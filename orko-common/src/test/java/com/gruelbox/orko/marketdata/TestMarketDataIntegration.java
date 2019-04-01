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

import static com.gruelbox.orko.db.TestingUtils.skipIfSlowTestsDisabled;
import static com.gruelbox.orko.exchange.Exchanges.BITMEX;
import static com.gruelbox.orko.marketdata.MarketDataType.BALANCE;
import static com.gruelbox.orko.marketdata.MarketDataType.OPEN_ORDERS;
import static com.gruelbox.orko.marketdata.MarketDataType.ORDERBOOK;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static com.gruelbox.orko.marketdata.MarketDataType.TRADES;
import static java.util.Collections.emptySet;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.simulated.AccountFactory;
import org.knowm.xchange.simulated.MatchingEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.exchange.AccountServiceFactory;
import com.gruelbox.orko.exchange.ExchangeConfiguration;
import com.gruelbox.orko.exchange.ExchangeServiceImpl;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.SafelyDispose;

import ch.qos.logback.classic.Level;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import jersey.repackaged.com.google.common.collect.Maps;

/**
 * Stack tests for {@link MarketDataSubscriptionManager}. Actually connects to exchanges.
 */
public class TestMarketDataIntegration {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestMarketDataIntegration.class);

  private static final TickerSpec binance = TickerSpec.builder().base("BTC").counter("USDT").exchange(Exchanges.BINANCE).build();
  private static final TickerSpec bitfinex = TickerSpec.builder().base("BTC").counter("USD").exchange(Exchanges.BITFINEX).build();
  private static final TickerSpec gdax = TickerSpec.builder().base("BTC").counter("USD").exchange(Exchanges.GDAX).build();
  private static final TickerSpec bittrex = TickerSpec.builder().base("BTC").counter("USDT").exchange(Exchanges.BITTREX).build();
  //private static final TickerSpec cryptopia = TickerSpec.builder().base("BTC").counter("USDT").exchange(Exchanges.CRYPTOPIA).build();
  private static final TickerSpec kucoin = TickerSpec.builder().base("BTC").counter("USDT").exchange(Exchanges.KUCOIN).build();
  private static final TickerSpec kraken = TickerSpec.builder().base("BTC").counter("USD").exchange(Exchanges.KRAKEN).build();
  private static final TickerSpec simulated = TickerSpec.builder().base("BTC").counter("USD").exchange(Exchanges.SIMULATED).build();

  private static final Set<MarketDataSubscription> subscriptions = FluentIterable.concat(
      FluentIterable.of(binance, bitfinex, gdax, bittrex, kraken, kucoin, simulated)
        .transformAndConcat(spec -> ImmutableSet.of(
          MarketDataSubscription.create(spec, TICKER),
          MarketDataSubscription.create(spec, ORDERBOOK),
          MarketDataSubscription.create(spec, TRADES)
        )),
      ImmutableSet.of(
        MarketDataSubscription.create(TickerSpec.builder().base("ETH").counter("USDT").exchange(Exchanges.BINANCE).build(), TRADES)
      )
    )
    .toSet();

  private ExchangeServiceImpl exchangeServiceImpl;
  private MarketDataSubscriptionManager marketDataSubscriptionManager;
  private ExchangeEventBus exchangeEventBus;
  private final NotificationService notificationService = mock(NotificationService.class);
  private AccountFactory accountFactory;
  private MatchingEngineFactory matchingEngineFactory;
  private OrkoConfiguration orkoConfiguration;


  @Before
  public void setup() throws TimeoutException {

    ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);

    orkoConfiguration = new OrkoConfiguration();
    orkoConfiguration.setExchanges(ImmutableMap.of(Exchanges.SIMULATED, new ExchangeConfiguration()));
    orkoConfiguration.getExchanges().get(Exchanges.SIMULATED).setApiKey("Test");

    accountFactory = new AccountFactory();
    matchingEngineFactory = new MatchingEngineFactory(accountFactory);

    orkoConfiguration.setLoopSeconds(2);
    exchangeServiceImpl = new ExchangeServiceImpl(orkoConfiguration,
        accountFactory,
        matchingEngineFactory);
    marketDataSubscriptionManager = new MarketDataSubscriptionManager(
      exchangeServiceImpl,
      orkoConfiguration,
      exchange -> exchangeServiceImpl.get(exchange).getTradeService(),
      new AccountServiceFactory() {
        @Override
        public AccountService getForExchange(String exchange) {
          return exchangeServiceImpl.get(exchange).getAccountService();
        }
      },
      notificationService
    );
    exchangeEventBus = new ExchangeEventBus(marketDataSubscriptionManager);
    marketDataSubscriptionManager.startAsync().awaitRunning(20, SECONDS);
  }

  @After
  public void tearDown() throws TimeoutException {
    marketDataSubscriptionManager.stopAsync().awaitTerminated(20, SECONDS);
  }

  @Test
  public void testBase() throws InterruptedException {
    marketDataSubscriptionManager.updateSubscriptions(emptySet());
  }

  @Test
  public void testSimulated() throws InterruptedException, IOException, TimeoutException {
    SimulatedOrderBookActivity simulator = new SimulatedOrderBookActivity(accountFactory, matchingEngineFactory);
    simulator.startAsync().awaitRunning(30, SECONDS);
    try {
      testSubscriptionsViaEventBus(ImmutableSet.of(
          MarketDataSubscription.create(simulated, TICKER),
          MarketDataSubscription.create(simulated, ORDERBOOK),
          MarketDataSubscription.create(simulated, TRADES),
          MarketDataSubscription.create(simulated, BALANCE),
          MarketDataSubscription.create(simulated, OPEN_ORDERS)
        ), 4);
      LOGGER.info("Test complete");
    } finally {
      simulator.stopAsync().awaitTerminated(30, SECONDS);
    }
  }

  @Test
  public void testSubscribeUnsubscribe() throws InterruptedException {

    skipIfSlowTestsDisabled();

    marketDataSubscriptionManager.updateSubscriptions(subscriptions);
    marketDataSubscriptionManager.updateSubscriptions(emptySet());
  }

  @Test
  public void testBitmexContracts() throws InterruptedException {

    skipIfSlowTestsDisabled();

    Set<CurrencyPair> pairs = exchangeServiceImpl.get(BITMEX).getExchangeMetaData().getCurrencyPairs().keySet();
    Set<TickerSpec> coins = FluentIterable.from(pairs)
        .filter(c -> !c.counter.getCurrencyCode().contains("_")) // TODO XChange bug
        .transform(c -> TickerSpec.builder()
            .base(c.base.getCurrencyCode())
            .counter(c.counter.getCurrencyCode())
            .exchange(BITMEX)
            .build())
        .toSet();

    System.out.println(coins);

    ImmutableSet<MarketDataSubscription> bitmexSubscriptions = FluentIterable.from(coins)
        .transformAndConcat(t -> ImmutableSet.of(
          MarketDataSubscription.create(t, TICKER),
          MarketDataSubscription.create(t, ORDERBOOK),
          MarketDataSubscription.create(t, TRADES)
        )).toSet();

    ImmutableMap<MarketDataSubscription, CountDownLatch> latchesBySubscriber = Maps.toMap(
        bitmexSubscriptions,
        sub -> new CountDownLatch(2)
      );

    Set<Disposable> disposables = null;

    marketDataSubscriptionManager.updateSubscriptions(bitmexSubscriptions);
    try {

      disposables = FluentIterable.from(bitmexSubscriptions).transform(sub ->
        getSubscription(marketDataSubscriptionManager, sub).subscribe(t -> latchesBySubscriber.get(sub).countDown())
      ).toSet();

      latchesBySubscriber.entrySet().stream().parallel().forEach(entry -> {
        MarketDataSubscription sub = entry.getKey();
        CountDownLatch latch = entry.getValue();
        try {
          assertTrue("No response for " + sub, latch.await(120, TimeUnit.SECONDS));
          System.out.println("Found responses for " + sub);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      });

    } finally {
      SafelyDispose.of(disposables);
      marketDataSubscriptionManager.updateSubscriptions(emptySet());
    }
  }

  @Test
  public void testSubscribePauseAndUnsubscribe() throws InterruptedException {

    skipIfSlowTestsDisabled();

    marketDataSubscriptionManager.updateSubscriptions(subscriptions);
    Thread.sleep(2500);
    marketDataSubscriptionManager.updateSubscriptions(emptySet());
  }

  @Test
  public void testSubscriptionsDirect() throws InterruptedException {

    skipIfSlowTestsDisabled();

    marketDataSubscriptionManager.updateSubscriptions(subscriptions);
    Set<Disposable> disposables = null;
    try {
      ImmutableMap<MarketDataSubscription, List<CountDownLatch>> latchesBySubscriber = Maps.toMap(
        subscriptions,
        sub -> ImmutableList.of(new CountDownLatch(2), new CountDownLatch(2))
      );
      disposables = FluentIterable.from(subscriptions).transformAndConcat(sub -> ImmutableSet.<Disposable>of(
        getSubscription(marketDataSubscriptionManager, sub).subscribe(t -> {
          latchesBySubscriber.get(sub).get(0).countDown();
        }),
        getSubscription(marketDataSubscriptionManager, sub).subscribe(t -> {
          latchesBySubscriber.get(sub).get(1).countDown();
        })
      )).toSet();
      latchesBySubscriber.entrySet().stream().parallel().forEach(entry -> {
        MarketDataSubscription sub = entry.getKey();
        List<CountDownLatch> latches = entry.getValue();
        try {
          assertTrue("Missing two responses (A) for " + sub, latches.get(0).await(120, TimeUnit.SECONDS));
          System.out.println("Found responses (A) for " + sub);
          assertTrue("Missing two responses (B) for " + sub, latches.get(1).await(1, TimeUnit.SECONDS));
          System.out.println("Found responses (B) for " + sub);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      });
    } finally {
      SafelyDispose.of(disposables);
      marketDataSubscriptionManager.updateSubscriptions(emptySet());
    }
  }

  @Test
  public void testSubscriptionsViaEventBus() throws InterruptedException {
    skipIfSlowTestsDisabled();
    testSubscriptionsViaEventBus(subscriptions, 2);
  }

  public void testSubscriptionsViaEventBus(Set<MarketDataSubscription> subscriptions, int count) throws InterruptedException {
    try (ExchangeEventSubscription subscription = exchangeEventBus.subscribe(subscriptions)) {
      ImmutableMap<MarketDataSubscription, List<CountDownLatch>> latchesBySubscriber = Maps.toMap(
        subscriptions,
        sub -> ImmutableList.of(new CountDownLatch(count), new CountDownLatch(count))
      );
      Set<Disposable> disposables = FluentIterable.from(subscriptions).transformAndConcat(sub -> ImmutableSet.<Disposable>of(
        getSubscription(subscription, sub).subscribe(t -> {
          latchesBySubscriber.get(sub).get(0).countDown();
        }),
        getSubscription(subscription, sub).subscribe(t -> {
          latchesBySubscriber.get(sub).get(1).countDown();
        })
      )).toSet();
      latchesBySubscriber.entrySet().stream().parallel().forEach(entry -> {
        MarketDataSubscription sub = entry.getKey();
        List<CountDownLatch> latches = entry.getValue();
        try {
          assertTrue("Missing " + count + " responses (A) for " + sub, latches.get(0).await(120, TimeUnit.SECONDS));
          System.out.println("Found responses (A) for " + sub);
          assertTrue("Missing " + count + " responses (B) for " + sub, latches.get(1).await(1, TimeUnit.SECONDS));
          System.out.println("Found responses (B) for " + sub);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      });
      SafelyDispose.of(disposables);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> Flowable<T> getSubscription(MarketDataSubscriptionManager manager, MarketDataSubscription sub) {
    switch (sub.type()) {
      case OPEN_ORDERS:
        return (Flowable<T>) manager.getOrderSnapshots().filter(o -> o.spec().equals(sub.spec()));
      case ORDERBOOK:
        return (Flowable<T>) manager.getOrderBookSnapshots().filter(o -> o.spec().equals(sub.spec()));
      case TICKER:
        return (Flowable<T>) manager.getTickers().filter(o -> o.spec().equals(sub.spec()));
      case TRADES:
        return (Flowable<T>) manager.getTrades().filter(o -> o.spec().equals(sub.spec()));
      case USER_TRADE:
        return (Flowable<T>) manager.getUserTrades().filter(o -> o.spec().equals(sub.spec()));
      case BALANCE:
        return (Flowable<T>) manager.getBalances().filter(b -> b.currency().equals(sub.spec().base()) || b.currency().equals(sub.spec().counter()));
      default:
        throw new IllegalArgumentException("Unknown market data type");
    }
  }

  @SuppressWarnings("unchecked")
  private <T> Flowable<T> getSubscription(ExchangeEventSubscription subscription, MarketDataSubscription sub) {
    switch (sub.type()) {
      case OPEN_ORDERS:
        return (Flowable<T>) subscription.getOrderSnapshots();
      case ORDERBOOK:
        return (Flowable<T>) subscription.getOrderBooks();
      case TICKER:
        return (Flowable<T>) subscription.getTickers();
      case TRADES:
        return (Flowable<T>) subscription.getTrades();
      case USER_TRADE:
        return (Flowable<T>) subscription.getUserTrades();
      case BALANCE:
        return (Flowable<T>) subscription.getBalances();
      case ORDER:
        return (Flowable<T>) subscription.getOrderChanges();
      default:
        throw new IllegalArgumentException("Unknown market data type");
    }
  }


  @Test
  public void testEventBusSubscriptionDifferentSubscriberInner() throws InterruptedException {

    skipIfSlowTestsDisabled();

    try (ExchangeEventSubscription otherSubscription = exchangeEventBus.subscribe()) {
      try (ExchangeEventSubscription subscription = exchangeEventBus.subscribe(subscriptions)) {
        AtomicBoolean called = new AtomicBoolean();
        Disposable disposable = otherSubscription.getTickers().subscribe(t -> called.set(true));
        Thread.sleep(10000);
        assertFalse(called.get());
        SafelyDispose.of(disposable);
      }
    }
  }


  @Test
  public void testEventBusSubscriptionDifferentSubscriberOuter() throws InterruptedException {

    skipIfSlowTestsDisabled();

    try (ExchangeEventSubscription subscription = exchangeEventBus.subscribe(subscriptions)) {
      try (ExchangeEventSubscription otherSubscription = exchangeEventBus.subscribe()) {
        AtomicBoolean called = new AtomicBoolean();
        Disposable disposable = otherSubscription.getTickers().subscribe(t -> called.set(true));
        Thread.sleep(10000);
        assertFalse(called.get());
        SafelyDispose.of(disposable);
      }
    }
  }


  @Test
  public void testEventBusSubscriptionSameSubscriber() throws InterruptedException {

    skipIfSlowTestsDisabled();

    try (ExchangeEventSubscription subscription = exchangeEventBus.subscribe(subscriptions)) {
      CountDownLatch called1 = new CountDownLatch(2);
      CountDownLatch called2 = new CountDownLatch(2);
      CountDownLatch called3 = new CountDownLatch(2);
      Disposable disposable1 = subscription.getTickers().throttleLast(200, TimeUnit.MILLISECONDS).subscribe(t -> {
        System.out.println(Thread.currentThread().getId() + " (A) received ticker: " + t);
        called1.countDown();
      });
      Disposable disposable2 = subscription.getTickers().throttleLast(200, TimeUnit.MILLISECONDS).subscribe(t -> {
        System.out.println(Thread.currentThread().getId() + " (B) received ticker: " + t);
        Thread.sleep(2000);
        called2.countDown();
      });
      Disposable disposable3 = subscription.getOrderBooks().throttleLast(1, TimeUnit.SECONDS).subscribe(t -> {
        System.out.println(Thread.currentThread().getId() + " (C) received order book: " + t.getClass().getSimpleName());
        called3.countDown();
      });
      assertTrue(called1.await(20, SECONDS));
      assertTrue(called2.await(20, SECONDS));
      assertTrue(called3.await(20, SECONDS));
      SafelyDispose.of(disposable1, disposable2, disposable3);
    }
  }

  @Test
  public void testEventBusMultipleSubscribersSameTicker() throws InterruptedException {

    skipIfSlowTestsDisabled();

    try (ExchangeEventSubscription subscription1 = exchangeEventBus.subscribe(MarketDataSubscription.create(binance, TICKER))) {
      try (ExchangeEventSubscription subscription2 = exchangeEventBus.subscribe(MarketDataSubscription.create(binance, TICKER))) {
        CountDownLatch called1 = new CountDownLatch(2);
        CountDownLatch called2 = new CountDownLatch(2);
        Disposable disposable1 = subscription1.getTickers().subscribe(t -> {
          System.out.println(Thread.currentThread().getId() + " (A) received: " + t);
          called1.countDown();
        });
        Disposable disposable2 = subscription2.getTickers().subscribe(t -> {
          System.out.println(Thread.currentThread().getId() + " (B) received: " + t);
          called2.countDown();
        });
        assertTrue(called1.await(20, SECONDS));
        assertTrue(called2.await(20, SECONDS));
        SafelyDispose.of(disposable1, disposable2);
      }
    }
  }
}
