/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.exchange;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.wiring.BackgroundProcessingConfiguration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.mockito.Mock;

@Slf4j
public class TestMarketDataSubscriptionManager {

  private static final int WAIT = 20;
  private static final String EXCHANGE1 = "exchange1";
  private static final String EXCHANGE2 = "exchange2";

  @Mock private ExchangeService exchangeService;
  @Mock private BackgroundProcessingConfiguration configuration;
  @Mock private TradeServiceFactory tradeServiceFactory;
  @Mock private AccountServiceFactory accountServiceFactory;
  @Mock private NotificationService notificationService;

  @Mock private Exchange exchangeOne;
  @Mock private Exchange exchangeTwo;

  private final CountDownLatch allExchangesBlocked = new CountDownLatch(2);
  private final CountDownLatch shutdown = new CountDownLatch(2);

  private final Set<Thread> threads = Sets.newConcurrentHashSet();

  private SubscriptionControllerLocalImpl subscriptionManager;

  @Before
  public void setup() {
    initMocks(this);
    when(exchangeService.getExchanges()).thenReturn(ImmutableList.of(EXCHANGE1, EXCHANGE2));
    when(exchangeService.get(EXCHANGE1)).thenReturn(exchangeOne);
    when(exchangeService.get(EXCHANGE2)).thenReturn(exchangeTwo);
    when(configuration.getLoopSeconds()).thenReturn(1);
    subscriptionManager =
        new SubscriptionControllerLocalImpl(
            exchangeService,
            configuration,
            tradeServiceFactory,
            accountServiceFactory,
            notificationService,
            new SubscriptionPublisher());
    subscriptionManager.setLifecycleListener(
        new LifecycleListener() {
          @Override
          public void onBlocked(String exchange) {
            log.info("{} blocked", exchange);
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
  public void testImmediateStartupShutdown() {
    subscriptionManager.start();
    subscriptionManager.stop();
  }

  @Test
  public void testControlledStartupShutdownFromBlockedState() throws InterruptedException {
    subscriptionManager.start();
    try {
      awaitAllExchangesBlocked();
    } finally {
      subscriptionManager.stop();
    }
  }

  @Test
  public void testInterrupt() throws InterruptedException {
    subscriptionManager.start();
    try {
      awaitAllExchangesBlocked();
      threads.forEach(Thread::interrupt);
      assertTrue(shutdown.await(WAIT, SECONDS));
    } finally {
      subscriptionManager.stop();
    }
  }

  private void awaitAllExchangesBlocked() throws InterruptedException {
    assertTrue(allExchangesBlocked.await(WAIT, SECONDS));
    Thread.sleep(500); // Just to be sure since there's a race condition here
  }
}
