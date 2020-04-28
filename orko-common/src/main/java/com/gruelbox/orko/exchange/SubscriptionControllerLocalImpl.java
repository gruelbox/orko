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
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.wiring.BackgroundProcessingConfiguration;
import io.dropwizard.lifecycle.Managed;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Maintains subscriptions to multiple exchanges' market data, using web sockets where it can and
 * polling where it can't, but this is abstracted away. All clients have access to reactive streams
 * of data which are persistent and recover in the event of disconnections/reconnections.
 */
@Singleton
@Slf4j
class SubscriptionControllerLocalImpl implements SubscriptionController, Managed {

  private final ExchangeService exchangeService;
  private final BackgroundProcessingConfiguration configuration;
  private final Map<String, RxServiceWrapper<ExchangePollLoop>> pollers;

  @Inject
  @VisibleForTesting
  public SubscriptionControllerLocalImpl(
      ExchangeService exchangeService,
      BackgroundProcessingConfiguration configuration,
      TradeServiceFactory tradeServiceFactory,
      AccountServiceFactory accountServiceFactory,
      NotificationService notificationService,
      SubscriptionPublisher publisher) {
    this.configuration = configuration;
    this.exchangeService = exchangeService;
    this.pollers =
        exchangeService.getExchanges().stream()
            .collect(toMap(identity(), e -> new RxServiceWrapper<>(new ExchangePollLoop(e,
                exchangeService.get(e),
                () -> accountServiceFactory.getForExchange(e),
                () -> tradeServiceFactory.getForExchange(e),
                exchangeService.rateController(e),
                notificationService,
                publisher,
                configuration.getLoopSeconds(),
                exchangeService.isAuthenticated(e)))));
    publisher.setController(this);
  }

  @Override
  public void start() {
    boolean started = Observable.fromIterable(pollers.values())
        .flatMapCompletable(poller ->
            poller.start()
                .doOnError(e -> log
                    .error("{} start failed with uncaught exception and will not restart",
                        poller.getDelegate().getExchangeName(), e))
                .onErrorComplete())
        .blockingAwait(configuration.getLoopSeconds() * 2, TimeUnit.SECONDS);
    if (!started) {
      throw new IllegalStateException("Failed to start pollers within time limit");
    }
  }

  @Override
  public void stop() {
    boolean stopped = Observable.fromIterable(pollers.values())
        .flatMapCompletable(poller ->
            poller.stop()
                .doOnError(e -> log
                    .error("{} stop failed with uncaught exception", poller.getDelegate().getExchangeName(), e))
                .onErrorComplete())
        .blockingAwait(configuration.getLoopSeconds() * 2, TimeUnit.SECONDS);
    if (!stopped) {
      throw new IllegalStateException("Failed to stop pollers within time limit");
    }
  }

  /**
   * Updates the subscriptions for the specified exchanges on the next loop tick. The delay is to
   * avoid a large number of new subscriptions in quick succession causing rate bans on exchanges.
   * Call with an empty set to cancel all subscriptions. None of the streams will return anything
   * until this is called, but there is no strict order in which they need to be called.
   *
   * @param subscriptions The subscriptions.
   */
  @Override
  public Completable updateSubscriptions(Set<MarketDataSubscription> subscriptions) {
    // Queue them up for each exchange's processing thread individually
    return Completable.fromRunnable(() -> {
      ImmutableListMultimap<String, MarketDataSubscription> byExchange =
          Multimaps.index(subscriptions, s -> s.spec().exchange());
      for (String exchangeName : exchangeService.getExchanges()) {
        pollers.get(exchangeName).getDelegate()
            .updateSubscriptions(FluentIterable.from(byExchange.get(exchangeName))
                .transform(MarketDataSubscription::toSubscription));
      }
    }).cache();
  }

  @VisibleForTesting
  void setLifecycleListener(LifecycleListener listener) {
    pollers.values().forEach(poller-> poller.getDelegate().setLifecycleListener(listener));
  }
}
