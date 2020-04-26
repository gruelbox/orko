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

import static java.util.Collections.emptySet;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.wiring.BackgroundProcessingConfiguration;
import io.reactivex.Completable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

/**
 * Maintains subscriptions to multiple exchanges' market data, using web sockets where it can and
 * polling where it can't, but this is abstracted away. All clients have access to reactive streams
 * of data which are persistent and recover in the event of disconnections/reconnections.
 */
@Singleton
@Slf4j
class SubscriptionControllerLocalImpl extends AbstractExecutionThreadService implements SubscriptionController {

  private final ExchangeService exchangeService;
  private final Map<String, ExchangeConfiguration> exchangeConfiguration;
  private LifecycleListener lifecycleListener = new LifecycleListener() {};
  private final Map<String, ExchangePollLoop> pollers;

  @Inject
  @VisibleForTesting
  public SubscriptionControllerLocalImpl(
      ExchangeService exchangeService,
      BackgroundProcessingConfiguration configuration,
      TradeServiceFactory tradeServiceFactory,
      AccountServiceFactory accountServiceFactory,
      NotificationService notificationService,
      SubscriptionPublisher publisher,
      Map<String, ExchangeConfiguration> exchangeConfiguration) {
    publisher.setController(this);
    this.exchangeService = exchangeService;
    this.pollers =
        exchangeService.getExchanges().stream()
            .collect(toMap(identity(), e -> new ExchangePollLoop(e,
                exchangeService.get(e),
                () -> accountServiceFactory.getForExchange(e),
                () -> tradeServiceFactory.getForExchange(e),
                exchangeService.rateController(e),
                notificationService,
                publisher,
                lifecycleListener,
                configuration.getLoopSeconds(),
                exchangeService.isAuthenticated(e))));
    this.exchangeConfiguration = exchangeConfiguration;
  }

  /**
   * Updates the subscriptions for the specified exchanges on the next loop tick. The delay is to
   * avoid a large number of new subscriptions in quick succession causing rate bans on exchanges.
   * Call with an empty set to cancel all subscriptions. None of the streams will return anything
   * until this is called, but there is no strict order in which they need to be called.
   *
   * @param subscriptions The subscriptions.
   */
  public Completable updateSubscriptions(Set<MarketDataSubscription> subscriptions) {
    // Queue them up for each exchange's processing thread individually
    return Completable.fromRunnable(() -> {
      ImmutableListMultimap<String, MarketDataSubscription> byExchange =
          Multimaps.index(subscriptions, s -> s.spec().exchange());
      for (String exchangeName : exchangeService.getExchanges()) {
        pollers.get(exchangeName).updateSubscriptions(byExchange.get(exchangeName));
      }
    }).cache();
  }

  @Override
  protected final void run() {
    Thread.currentThread().setName(getClass().getSimpleName());
    log.info("{} started", this);
    try {
      doRun();
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
    } catch (InterruptedException e) {
      log.error("{} stopping due to interrupt", this, e);
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      log.error("{} stopping due to uncaught exception", this, e);
    } finally {
      updateSubscriptions(emptySet()).blockingAwait();
      log.info("{} stopped", this);
      lifecycleListener.onStopMain();
    }
  }

  @VisibleForTesting
  void setLifecycleListener(LifecycleListener listener) {
    this.lifecycleListener = listener;
  }

  private void doRun() throws InterruptedException {
    ExecutorService threadPool =
        Executors.newFixedThreadPool(exchangeService.getExchanges().size());
    try {
      try {
        submitExchangesAndWaitForCompletion(threadPool);
        log.info("{} stopping; all exchanges have shut down", this);
      } catch (InterruptedException e) {
        throw e;
      } catch (Exception e) {
        log.error(this + " stopping due to uncaught exception", e);
      }
    } finally {
      threadPool.shutdownNow();
    }
  }

  @Override
  protected void triggerShutdown() {
    super.triggerShutdown();
    pollers.values().forEach(ExchangePollLoop::stop);
  }

  private void submitExchangesAndWaitForCompletion(ExecutorService threadPool)
      throws InterruptedException {
    Map<String, Future<?>> futures = new HashMap<>();
    for (String exchange : exchangeService.getExchanges()) {
      if (exchangeConfiguration.getOrDefault(exchange, new ExchangeConfiguration()).isEnabled()) {
        futures.put(exchange, threadPool.submit(pollers.get(exchange)));
      }
    }
    for (Entry<String, Future<?>> entry : futures.entrySet()) {
      try {
        entry.getValue().get();
      } catch (ExecutionException e) {
        log.error(entry.getKey() + " failed with uncaught exception and will not restart", e);
      }
    }
  }

}
