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

import static java.util.Collections.emptySet;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.service.account.AccountService;
import org.slf4j.LoggerFactory;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.exchange.AccountServiceFactory;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.util.SafelyDispose;

import ch.qos.logback.classic.Level;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import jersey.repackaged.com.google.common.collect.Maps;

public abstract class AbstractMarketDataFullStackTest {

  protected ExchangeService exchangeService;
  protected MarketDataSubscriptionManager marketDataSubscriptionManager;
  protected ExchangeEventBus exchangeEventBus;
  protected final NotificationService notificationService = mock(NotificationService.class);
  protected OrkoConfiguration orkoConfiguration;


  @Before
  public void setup() throws TimeoutException {

    ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);

    orkoConfiguration = buildConfig();
    exchangeService = buildExchangeService();
    marketDataSubscriptionManager = new MarketDataSubscriptionManager(
      exchangeService,
      orkoConfiguration,
      exchange -> exchangeService.get(exchange).getTradeService(),
      new AccountServiceFactory() {
        @Override
        public AccountService getForExchange(String exchange) {
          return exchangeService.get(exchange).getAccountService();
        }
      },
      notificationService
    );
    exchangeEventBus = new ExchangeEventBus(marketDataSubscriptionManager);
    marketDataSubscriptionManager.startAsync().awaitRunning(20, SECONDS);
  }

  protected OrkoConfiguration buildConfig() {
    OrkoConfiguration result = new OrkoConfiguration();
    result.setLoopSeconds(2);
    return result;
  }

  protected abstract ExchangeService buildExchangeService();

  protected abstract Set<MarketDataSubscription> subscriptions();

  protected abstract MarketDataSubscription ticker();

  @After
  public void tearDown() throws TimeoutException {
    marketDataSubscriptionManager.stopAsync().awaitTerminated(20, SECONDS);
  }

  @Test
  public void testBase() throws InterruptedException {
    marketDataSubscriptionManager.updateSubscriptions(emptySet());
  }

  @Test
  public void testSubscribeUnsubscribe() throws InterruptedException {
    marketDataSubscriptionManager.updateSubscriptions(subscriptions());
    marketDataSubscriptionManager.updateSubscriptions(emptySet());
  }

  @Test
  public void testSubscribePauseAndUnsubscribe() throws InterruptedException {
    marketDataSubscriptionManager.updateSubscriptions(subscriptions());
    Thread.sleep(2500);
    marketDataSubscriptionManager.updateSubscriptions(emptySet());
  }

  @Test
  public void testSubscriptionsDirect() throws InterruptedException {
    Set<MarketDataSubscription> subscriptions = subscriptions();
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
    testSubscriptionsViaEventBus(subscriptions(), 2);
  }

  private void testSubscriptionsViaEventBus(Set<MarketDataSubscription> subscriptions, int count) throws InterruptedException {
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

  @Test
  public void testEventBusSubscriptionDifferentSubscriberInner() throws InterruptedException {
    try (ExchangeEventSubscription otherSubscription = exchangeEventBus.subscribe()) {
      try (ExchangeEventSubscription subscription = exchangeEventBus.subscribe(subscriptions())) {
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
    try (ExchangeEventSubscription subscription = exchangeEventBus.subscribe(subscriptions())) {
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
    try (ExchangeEventSubscription subscription = exchangeEventBus.subscribe(subscriptions())) {
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
  public void testEventBusMultipleSubscribersSameThing() throws InterruptedException {
    try (ExchangeEventSubscription subscription1 = exchangeEventBus.subscribe(ticker())) {
      try (ExchangeEventSubscription subscription2 = exchangeEventBus.subscribe(ticker())) {
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
}
