package com.gruelbox.orko.marketdata;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.mockito.Mock;

import com.google.common.collect.ImmutableList;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.exchange.AccountServiceFactory;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.exchange.TradeServiceFactory;
import com.gruelbox.orko.marketdata.MarketDataSubscriptionManager.LifecycleListener;
import com.gruelbox.orko.notification.NotificationService;

import jersey.repackaged.com.google.common.collect.Sets;

public class TestMarketDataSubscriptionManager {

  private static final int WAIT = 20;
  private static final String EXCHANGE1 = "exchange1";
  private static final String EXCHANGE2 = "exchange2";

  @Mock private ExchangeService exchangeService;
  @Mock private OrkoConfiguration configuration;
  @Mock private TradeServiceFactory tradeServiceFactory;
  @Mock private AccountServiceFactory accountServiceFactory;
  @Mock private NotificationService notificationService;

  @Mock private Exchange exchangeOne;
  @Mock private Exchange exchangeTwo;

  private final CountDownLatch allExchangesBlocked = new CountDownLatch(2);
  private final CountDownLatch shutdown = new CountDownLatch(3);

  private final Set<Thread> threads = Sets.newConcurrentHashSet();

  private MarketDataSubscriptionManager subscriptionManager;

  @Before
  public void setup() throws TimeoutException {
    initMocks(this);
    when(exchangeService.getExchanges()).thenReturn(ImmutableList.of(EXCHANGE1, EXCHANGE2));
    when(exchangeService.get(EXCHANGE1)).thenReturn(exchangeOne);
    when(exchangeService.get(EXCHANGE2)).thenReturn(exchangeTwo);
    subscriptionManager = new MarketDataSubscriptionManager(exchangeService, configuration, tradeServiceFactory,
        accountServiceFactory, notificationService);
    subscriptionManager.setLifecycleListener(new LifecycleListener() {
      @Override
      public void onBlocked(String exchange) {
        threads.add(Thread.currentThread());
        allExchangesBlocked.countDown();
      }

      @Override
      public void onStop(String exchange) {
        shutdown.countDown();
      }

      @Override
      public void onStopMain() {
        shutdown.countDown();
      }
    });
  }

  @Test
  public void testImmediateStartupShutdown() throws TimeoutException {
    subscriptionManager.startAsync().awaitRunning(WAIT, SECONDS);
    subscriptionManager.stopAsync().awaitTerminated(WAIT, SECONDS);
  }

  @Test
  public void testControlledStartupShutdownFromBlockedState() throws TimeoutException, InterruptedException {
    subscriptionManager.startAsync().awaitRunning(WAIT, SECONDS);
    try {
      awaitAllExchangesBlocked();
    } finally {
      subscriptionManager.stopAsync().awaitTerminated(WAIT, SECONDS);
    }
  }

  @Test
  public void testInterrupt() throws TimeoutException, InterruptedException {
    subscriptionManager.startAsync().awaitRunning(WAIT, SECONDS);
    try {
      awaitAllExchangesBlocked();
      threads.forEach(Thread::interrupt);
      assertTrue(shutdown.await(WAIT, SECONDS));
    } finally {
      subscriptionManager.stopAsync().awaitTerminated(WAIT, SECONDS);
    }
  }

  private void awaitAllExchangesBlocked() throws InterruptedException {
    assertTrue(allExchangesBlocked.await(WAIT, SECONDS));
    Thread.sleep(500); // Just to be sure since there's a race condition here
  }
}