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
package com.gruelbox.orko.monitor;

import static com.gruelbox.orko.exchange.Exchanges.BINANCE;
import static com.gruelbox.orko.exchange.Exchanges.BITFINEX;
import static com.gruelbox.orko.exchange.Exchanges.GDAX;
import static com.gruelbox.orko.exchange.MarketDataType.TRADES;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.exchange.ExchangeEventRegistry;
import com.gruelbox.orko.exchange.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.exchange.MarketDataSubscription;
import com.gruelbox.orko.exchange.TradeEvent;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.SafelyClose;
import com.gruelbox.orko.util.SafelyDispose;
import io.dropwizard.lifecycle.Managed;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
final class ExchangeSocketHealthMonitor implements Managed {

  private static final int MINUTES_BEFORE_WARNING = 10;

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeSocketHealthMonitor.class);

  private final ExchangeEventRegistry exchangeEventRegistry;
  private final Map<String, Stopwatch> stopwatches;
  private final NotificationService notificationService;
  private final Observable<?> interval;
  private final Consumer<? super Throwable> onError;

  private ExchangeEventSubscription subscription;
  private Disposable trades;
  private Disposable poll;

  @Inject
  ExchangeSocketHealthMonitor(
      ExchangeEventRegistry exchangeEventRegistry, NotificationService notificationService) {
    this(
        exchangeEventRegistry,
        notificationService,
        Observable.interval(10, TimeUnit.MINUTES),
        Ticker.systemTicker(),
        t -> LOGGER.error(t.getMessage(), t));
  }

  @VisibleForTesting
  ExchangeSocketHealthMonitor(
      ExchangeEventRegistry exchangeEventRegistry,
      NotificationService notificationService,
      Observable<?> interval,
      Ticker ticker,
      Consumer<? super Throwable> onError) {
    this.exchangeEventRegistry = exchangeEventRegistry;
    this.notificationService = notificationService;
    this.interval = interval;
    this.onError = onError;
    this.stopwatches =
        ImmutableMap.of(
            BINANCE, Stopwatch.createUnstarted(ticker),
            BITFINEX, Stopwatch.createUnstarted(ticker),
            GDAX, Stopwatch.createUnstarted(ticker));
  }

  @Override
  public void start() throws Exception {
    stopwatches.values().forEach(Stopwatch::start);
    subscription =
        exchangeEventRegistry.subscribe(
            MarketDataSubscription.create(
                TickerSpec.builder().exchange(BINANCE).base("BTC").counter("USDT").build(), TRADES),
            MarketDataSubscription.create(
                TickerSpec.builder().exchange(BITFINEX).base("BTC").counter("USD").build(), TRADES),
            MarketDataSubscription.create(
                TickerSpec.builder().exchange(GDAX).base("BTC").counter("USD").build(), TRADES));
    trades = subscription.getTrades().subscribe(this::onTrade, onError::accept);
    poll = interval.subscribe(i -> runOneIteration(), onError::accept);
  }

  private void onTrade(TradeEvent event) {
    Stopwatch stopwatch = stopwatches.get(event.spec().exchange());
    if (stopwatch != null) {
      reset(stopwatch);
    }
  }

  private void runOneIteration() {
    runOneIteration(BINANCE);
    runOneIteration(BITFINEX);
    runOneIteration(GDAX);
  }

  private void runOneIteration(String exchange) {
    Stopwatch stopwatch = stopwatches.get(exchange);
    long elapsed = stopwatch.elapsed(TimeUnit.MINUTES);
    String name = Exchanges.name(exchange);
    if (elapsed >= MINUTES_BEFORE_WARNING) {
      notificationService.error(
          name + " trade stream has not sent a trade for " + MINUTES_BEFORE_WARNING + "m");
      reset(stopwatch);
    } else {
      LOGGER.debug("{} socket healthy", name);
    }
  }

  private void reset(Stopwatch stopwatch) {
    synchronized (stopwatch) {
      stopwatch.reset().start();
    }
  }

  @Override
  public void stop() throws Exception {
    SafelyDispose.of(poll, trades);
    SafelyClose.the(subscription);
  }
}
