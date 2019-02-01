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

package com.gruelbox.orko.exchange;

import static com.gruelbox.orko.exchange.Exchanges.BINANCE;
import static com.gruelbox.orko.marketdata.MarketDataType.TRADES;
import static io.reactivex.schedulers.Schedulers.single;
import static java.lang.System.currentTimeMillis;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.marketdata.MarketDataSubscription;
import com.gruelbox.orko.marketdata.TradeEvent;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.SafelyClose;
import com.gruelbox.orko.util.SafelyDispose;

import io.dropwizard.lifecycle.Managed;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

@Singleton
final class MonitorExchangeSocketHealth implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorExchangeSocketHealth.class);

  private final ExchangeEventRegistry exchangeEventRegistry;
  private final AtomicLong lastTradeTime = new AtomicLong();
  private final NotificationService notificationService;

  private ExchangeEventSubscription subscription;
  private Disposable trades;
  private Disposable poll;

  @Inject
  MonitorExchangeSocketHealth(ExchangeEventRegistry exchangeEventRegistry, NotificationService notificationService) {
    this.exchangeEventRegistry = exchangeEventRegistry;
    this.notificationService = notificationService;
  }

  @Override
  public void start() throws Exception {
    lastTradeTime.set(currentTimeMillis());
    subscription = exchangeEventRegistry.subscribe(
      MarketDataSubscription.create(TickerSpec.builder().exchange(BINANCE).base("BTC").counter("USDT").build(), TRADES)
    );
    trades = subscription.getTrades().forEach(t -> onTrade(t));
    poll = Observable.interval(10, TimeUnit.MINUTES)
        .observeOn(single())
        .subscribe(i -> runOneIteration());
  }

  private void onTrade(TradeEvent t) {
    lastTradeTime.set(currentTimeMillis());
  }

  private void runOneIteration() {
    long elapsed = currentTimeMillis() - lastTradeTime.get();
    if (elapsed > TimeUnit.MINUTES.toMillis(10)) {
      notificationService.error("Binance trade stream has not sent a BTC/USDT trade for " + TimeUnit.MILLISECONDS.toMinutes(elapsed) + "m");
    } else {
      LOGGER.info("Binance socket healthy");
    }
  }

  @Override
  public void stop() throws Exception {
    SafelyDispose.of(poll, trades);
    SafelyClose.the(subscription);
  }
}