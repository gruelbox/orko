package com.gruelbox.orko.exchange;

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
      MarketDataSubscription.create(TickerSpec.builder().exchange(Exchanges.BINANCE).base("BTC").counter("USDT").build(), TRADES)
    );
    disposable = subscription.getTrades().forEach(t -> lastTradeTime.set(currentTimeMillis()));
    poll = Observable.interval(10, TimeUnit.MINUTES)
        .observeOn(single())
        .subscribe(i -> runOneIteration());
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