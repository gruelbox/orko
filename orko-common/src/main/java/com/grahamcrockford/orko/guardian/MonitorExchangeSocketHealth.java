package com.grahamcrockford.orko.guardian;

import static com.grahamcrockford.orko.marketdata.MarketDataType.TRADES;
import static java.lang.System.currentTimeMillis;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.marketdata.ExchangeEventRegistry;
import com.grahamcrockford.orko.marketdata.ExchangeEventRegistry.ExchangeEventSubscription;
import com.grahamcrockford.orko.marketdata.MarketDataSubscription;
import com.grahamcrockford.orko.notification.NotificationService;
import com.grahamcrockford.orko.spi.TickerSpec;
import com.grahamcrockford.orko.util.SafelyClose;
import com.grahamcrockford.orko.util.SafelyDispose;

import io.dropwizard.lifecycle.Managed;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

@Singleton
final class MonitorExchangeSocketHealth implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorExchangeSocketHealth.class);

  private final ExchangeEventRegistry exchangeEventRegistry;
  private final AtomicLong lastTradeTime = new AtomicLong();
  private volatile Disposable disposable;
  private volatile Disposable poll;
  private final NotificationService notificationService;
  private ExchangeEventSubscription subscription;

  @Inject
  MonitorExchangeSocketHealth(ExchangeEventRegistry exchangeEventRegistry, NotificationService notificationService) {
    this.exchangeEventRegistry = exchangeEventRegistry;
    this.notificationService = notificationService;
  }

  @Override
  public void start() throws Exception {
    lastTradeTime.set(currentTimeMillis());
    subscription = exchangeEventRegistry.subscribe(
      MarketDataSubscription.create(TickerSpec.builder().exchange("binance").base("BTC").counter("USDT").build(), TRADES)
    );
    disposable = subscription.getTrades().forEach(t -> lastTradeTime.set(currentTimeMillis()));
    poll = Observable.interval(10, TimeUnit.MINUTES).subscribe(i -> runOneIteration());
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
    SafelyDispose.of(poll, disposable);
    SafelyClose.the(subscription);
  }
}