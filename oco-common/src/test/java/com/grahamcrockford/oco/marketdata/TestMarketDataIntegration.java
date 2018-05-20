package com.grahamcrockford.oco.marketdata;

import static com.grahamcrockford.oco.marketdata.MarketDataType.TICKER;
import static java.util.Collections.emptySet;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.exchange.ExchangeServiceImpl;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.util.Sleep;

import ch.qos.logback.classic.Level;
import io.reactivex.disposables.Disposable;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import jersey.repackaged.com.google.common.collect.Maps;

/**
 * Stack tests for {@link MarketDataSubscriptionManager}. Actually connects to exchanges.
 */
public class TestMarketDataIntegration {

  private static final TickerSpec binance = TickerSpec.builder().base("BTC").counter("USDT").exchange("binance").build();
  private static final TickerSpec bitfinex = TickerSpec.builder().base("BTC").counter("USD").exchange("bitfinex").build();
  private static final TickerSpec gdax = TickerSpec.builder().base("BTC").counter("USD").exchange("gdax").build();
  private static final TickerSpec bittrex = TickerSpec.builder().base("BTC").counter("USDT").exchange("bittrex").build();
  private static final TickerSpec kucoin = TickerSpec.builder().base("BTC").counter("USDT").exchange("kucoin").build();
  private static final Set<MarketDataSubscription> subscriptions = FluentIterable.of(kucoin, binance, bitfinex, gdax, bittrex)
    .transformAndConcat(spec -> ImmutableSet.of(
      MarketDataSubscription.create(spec, MarketDataType.TICKER),
      MarketDataSubscription.create(spec, MarketDataType.ORDERBOOK)
    ))
    .toSet();

  private MarketDataSubscriptionManager marketDataSubscriptionManager;
  private ExchangeEventBus exchangeEventBus;
  private Sleep sleep;


  @Before
  public void setup() throws TimeoutException {

    ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);

    OcoConfiguration ocoConfiguration = new OcoConfiguration();
    ocoConfiguration.setLoopSeconds(2);
    sleep = new Sleep(ocoConfiguration);
    ExchangeServiceImpl exchangeServiceImpl = new ExchangeServiceImpl(ocoConfiguration);
    marketDataSubscriptionManager = new MarketDataSubscriptionManager(
      exchangeServiceImpl,
      sleep,
      null
    );
    exchangeEventBus = new ExchangeEventBus(marketDataSubscriptionManager);
    marketDataSubscriptionManager.startAsync().awaitRunning(5, SECONDS);
  }

  @After
  public void tearDown() throws TimeoutException {
    marketDataSubscriptionManager.stopAsync().awaitTerminated(5, SECONDS);
  }

  @Test
  public void testBase() throws InterruptedException {
    marketDataSubscriptionManager.updateSubscriptions(emptySet());
  }

  @Test
  public void testSubscribeUnsubscribe() throws InterruptedException {
    marketDataSubscriptionManager.updateSubscriptions(subscriptions);
    marketDataSubscriptionManager.updateSubscriptions(emptySet());
  }

  @Test
  public void testSubscriptions() throws InterruptedException {
    marketDataSubscriptionManager.updateSubscriptions(subscriptions);
    try {
      ImmutableMap<MarketDataSubscription, List<CountDownLatch>> latchesBySubscriber = Maps.toMap(
        subscriptions,
        sub -> ImmutableList.of(new CountDownLatch(2), new CountDownLatch(2))
      );
      Set<Disposable> disposables = FluentIterable.from(subscriptions).transformAndConcat(sub -> ImmutableSet.<Disposable>of(
        marketDataSubscriptionManager.getSubscription(sub).subscribe(t -> {
          String desc = t.toString();
          if (desc.length() > 120) desc = desc.substring(0, 120);
          System.out.println("A received " + desc);
          latchesBySubscriber.get(sub).get(0).countDown();
        }),
        marketDataSubscriptionManager.getSubscription(sub).subscribe(t -> {
          String desc = t.toString();
          if (desc.length() > 120) desc = desc.substring(0, 120);
          System.out.println("B received " + desc);
          latchesBySubscriber.get(sub).get(1).countDown();
        })
      )).toSet();
      latchesBySubscriber.forEach((sub, latches) -> {
        try {
          assertTrue("Missing two responses (A) for " + sub, latches.get(0).await(60, TimeUnit.SECONDS));
          System.out.println("Found responses (A) for " + sub);
          assertTrue("Missing two responses (B) for " + sub, latches.get(1).await(1, TimeUnit.SECONDS));
          System.out.println("Found responses (B) for " + sub);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      });
      disposables.forEach(Disposable::dispose);
    } finally {
      marketDataSubscriptionManager.updateSubscriptions(emptySet());
    }
  }


  @Test
  public void testEventBusSubscriptionDifferentSubscriber() throws InterruptedException {
    exchangeEventBus.changeSubscriptions("ME", subscriptions);
    try {
      AtomicBoolean called = new AtomicBoolean();
      Disposable disposable = exchangeEventBus.getTickers("NOTME").subscribe(t -> called.set(true));
      Thread.sleep(10000);
      assertFalse(called.get());
      disposable.dispose();
    } finally {
      exchangeEventBus.clearSubscriptions("ME");
    }
  }


  @Test
  public void testEventBusSubscriptionSameSubscriber() throws InterruptedException {
    exchangeEventBus.changeSubscriptions("ME", subscriptions);
    try {
      CountDownLatch called1 = new CountDownLatch(2);
      CountDownLatch called2 = new CountDownLatch(2);
      CountDownLatch called3 = new CountDownLatch(2);
      Disposable disposable1 = exchangeEventBus.getTickers("ME").throttleLast(200, TimeUnit.MILLISECONDS).subscribe(t -> {
        System.out.println(Thread.currentThread().getId() + " (A) received ticker: " + t);
        called1.countDown();
      });
      Disposable disposable2 = exchangeEventBus.getTickers("ME").throttleLast(200, TimeUnit.MILLISECONDS).subscribe(t -> {
        System.out.println(Thread.currentThread().getId() + " (B) received ticker: " + t);
        sleep.sleep();
        called2.countDown();
      });
      Disposable disposable3 = exchangeEventBus.getOrderBooks("ME").throttleLast(1, TimeUnit.SECONDS).subscribe(t -> {
        System.out.println(Thread.currentThread().getId() + " (C) received order book: " + t.getClass().getSimpleName());
        called3.countDown();
      });
      assertTrue(called1.await(20, SECONDS));
      assertTrue(called2.await(20, SECONDS));
      assertTrue(called3.await(20, SECONDS));
      disposable1.dispose();
      disposable2.dispose();
      disposable3.dispose();
    } finally {
      exchangeEventBus.clearSubscriptions("ME");
    }
  }


  @Test
  public void testEventBusMultipleSubscribersSameTicker() throws InterruptedException {
    exchangeEventBus.changeSubscriptions("1", ImmutableSet.of(MarketDataSubscription.create(binance, TICKER)));
    try {
      exchangeEventBus.changeSubscriptions("2", ImmutableSet.of(MarketDataSubscription.create(binance, TICKER)));
      try {
        CountDownLatch called1 = new CountDownLatch(2);
        CountDownLatch called2 = new CountDownLatch(2);
        Disposable disposable1 = exchangeEventBus.getTickers("1").subscribe(t -> {
          System.out.println(Thread.currentThread().getId() + " (A) received: " + t);
          called1.countDown();
        });
        Disposable disposable2 = exchangeEventBus.getTickers("2").subscribe(t -> {
          System.out.println(Thread.currentThread().getId() + " (B) received: " + t);
          called2.countDown();
        });
        assertTrue(called1.await(20, SECONDS));
        assertTrue(called2.await(20, SECONDS));
        disposable1.dispose();
        disposable2.dispose();
      } finally {
        exchangeEventBus.clearSubscriptions("2");
      }
    } finally {
      exchangeEventBus.clearSubscriptions("1");
    }
  }
}