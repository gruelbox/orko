package com.grahamcrockford.oco.marketdata;

import static com.grahamcrockford.oco.marketdata.MarketDataType.OPEN_ORDERS;
import static com.grahamcrockford.oco.marketdata.MarketDataType.TICKER;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.spi.TickerSpec;


@Singleton
class ExchangeEventBus implements ExchangeEventRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeEventBus.class);

  private final ExecutorService executorService;

  private final Set<MarketDataSubscription> allSubscriptions = Sets.newHashSet();
  private final Multimap<String, MarketDataSubscription> subscriptionsBySubscriber = MultimapBuilder.hashKeys().hashSetValues().build();
  private final Multimap<MarketDataSubscription, CallbackDef<TickerEvent>> tickerListeners = MultimapBuilder.hashKeys().hashSetValues().build();
  private final Multimap<MarketDataSubscription, CallbackDef<OpenOrdersEvent>> openOrdersListeners = MultimapBuilder.hashKeys().hashSetValues().build();

  private final StampedLock rwLock = new StampedLock();
  private final MarketDataSubscriptionManager marketDataSubscriptionManager;

  @Inject
  ExchangeEventBus(EventBus eventBus, ExecutorService executorService, MarketDataSubscriptionManager marketDataSubscriptionManager) {
    this.executorService = executorService;
    this.marketDataSubscriptionManager = marketDataSubscriptionManager;
    eventBus.register(this);
  }

  @Override
  public void registerTicker(TickerSpec spec, String subscriberId, Consumer<TickerEvent> callback) {
    withWriteLock(() -> {
      if (subscribe(subscriberId, MarketDataSubscription.create(spec, TICKER), callback, tickerListeners)) {
        updateSubscriptions();
      }
    });
  }

  @Override
  public void unregisterTicker(TickerSpec spec, String subscriberId) {
    withWriteLock(() -> {
      if (unsubscribe(subscriberId, MarketDataSubscription.create(spec, TICKER), tickerListeners)) {
        updateSubscriptions();
      }
    });
  }

  @Override
  public void changeSubscriptions(Set<MarketDataSubscription> targetSubscriptions,
                                  String subscriberId,
                                  Consumer<TickerEvent> tickerCallback,
                                  Consumer<OpenOrdersEvent> openOrdersCallback) {

    LOGGER.info("Changing subscriptions for subscriber {} to {}", subscriberId, targetSubscriptions);

    withWriteLock(() -> {

      boolean updated = false;

      Set<MarketDataSubscription> currentForSubscriber = ImmutableSet.copyOf(subscriptionsBySubscriber.get(subscriberId));
      Set<MarketDataSubscription> toRemove = Sets.difference(currentForSubscriber, targetSubscriptions);
      Set<MarketDataSubscription> toAdd = Sets.difference(targetSubscriptions, currentForSubscriber);

      for (MarketDataSubscription sub : toRemove) {
        switch (sub.type()) {
          case OPEN_ORDERS:
            if (unsubscribe(subscriberId, sub, openOrdersListeners))
              updated = true;
            break;
          case TICKER:
            if (unsubscribe(subscriberId, sub, tickerListeners))
              updated = true;
            break;
          default:
            throw new UnsupportedOperationException("Unsupported market data type:" + sub.type());
        }
      }

      for (MarketDataSubscription sub : toAdd) {
        switch (sub.type()) {
          case OPEN_ORDERS:
            if (subscribe(subscriberId, sub, openOrdersCallback, openOrdersListeners))
              updated = true;
            break;
          case TICKER:
            if (subscribe(subscriberId, sub, tickerCallback, tickerListeners))
              updated = true;
            break;
          default:
            throw new UnsupportedOperationException("Unsupported market data type:" + sub.type());
        }
      }

      if (updated) {
        updateSubscriptions();
      }
    });
  }

  private <T> boolean subscribe(String subscriberId, MarketDataSubscription subscription, Consumer<T> callback, Multimap<MarketDataSubscription, CallbackDef<T>> listeners) {
    if (subscriptionsBySubscriber.put(subscriberId, subscription)) {
      listeners.put(subscription, new CallbackDef<T>(subscriberId, callback));
      return allSubscriptions.add(subscription);
    }
    return false;
  }

  private <T> boolean unsubscribe(String subscriberId, MarketDataSubscription subscription, Multimap<MarketDataSubscription, CallbackDef<T>> listeners) {
    if (subscriptionsBySubscriber.remove(subscriberId, subscription)) {
      listeners.remove(subscription, new CallbackDef<T>(subscriberId, null));
      if (!listeners.containsKey(subscription)) {
        allSubscriptions.remove(subscription);
        return true;
      }
    }
    return false;
  }

  private void updateSubscriptions() {
    marketDataSubscriptionManager.updateSubscriptions(allSubscriptions);
  }

  @Subscribe
  public void tickerEvent(TickerEvent tickerEvent) {
    LOGGER.debug("Ticker event: {}", tickerEvent);
    long stamp = rwLock.readLock();
    try {
      tickerListeners.get(MarketDataSubscription.create(tickerEvent.spec(), TICKER))
        .forEach(c -> executorService.execute(() -> c.process(tickerEvent)));
    } finally {
      rwLock.unlockRead(stamp);
    }
  }

  @Subscribe
  public void openOrdersEvent(OpenOrdersEvent openOrdersEvent) {
    LOGGER.debug("Open orders event: {}", openOrdersEvent);
    long stamp = rwLock.readLock();
    try {
      openOrdersListeners.get(MarketDataSubscription.create(openOrdersEvent.spec(), OPEN_ORDERS))
        .forEach(c -> executorService.execute(() -> c.process(openOrdersEvent)));
    } finally {
      rwLock.unlockRead(stamp);
    }
  }

  private void withWriteLock(Runnable runnable) {
    long stamp = rwLock.writeLock();
    try {
      runnable.run();
    } finally {
      rwLock.unlockWrite(stamp);
    }
  }
}


/**
 * Callback placeholder.
 */
final class CallbackDef<T> {

  private final String id;
  private final Consumer<T> callback;
  private final Lock lock = new ReentrantLock();

  CallbackDef(String id, Consumer<T> callback) {
    super();
    this.id = id;
    this.callback = callback;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    @SuppressWarnings("unchecked")
    CallbackDef<T> other = (CallbackDef<T>) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "CallbackDef [id=" + id + "]";
  }

  void process(T tickerEvent) {
    // Prevent passing more tickers to the same job if it's still processing
    // an old one.
    if (!lock.tryLock())
      return;
    try {
      callback.accept(tickerEvent);
    } finally {
      lock.unlock();
    }
  }
}