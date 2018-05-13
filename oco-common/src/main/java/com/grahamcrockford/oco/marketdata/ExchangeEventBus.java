package com.grahamcrockford.oco.marketdata;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
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
  private final Multimap<String, TickerSpec> byExchange = MultimapBuilder.hashKeys().hashSetValues().build();
  private final Multimap<TickerSpec, CallbackDef> listeners = MultimapBuilder.hashKeys().hashSetValues().build();
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
      listeners.put(spec, new CallbackDef(subscriberId, callback));
      if (byExchange.put(spec.exchange(), spec)) {
        updateSubscriptions();
      }
    });
  }

  @Override
  public void unregisterTicker(TickerSpec spec, String subscriberId) {
    withWriteLock(() -> {
      if (listeners.remove(spec, new CallbackDef(subscriberId, null)) && !listeners.containsKey(spec)) {
        byExchange.remove(spec.exchange(), spec);
        updateSubscriptions();
      }
    });
  }

  @Override
  public void changeRegisteredTickers(Iterable<TickerSpec> targetTickers, String subscriberId, Consumer<TickerEvent> callback) {
    LOGGER.info("Changing subscriptions for subscriber {} to {}", subscriberId, targetTickers);
    CallbackDef callbackDef = new CallbackDef(subscriberId, callback);
    Set<TickerSpec> targetSet = Sets.newHashSet(targetTickers);
    withWriteLock(() -> {

      // Remove any tickers for this job we don't want anymore
      boolean updated = false;
      Iterator<Entry<TickerSpec, CallbackDef>> listenerIterator = listeners.entries().iterator();
      while (listenerIterator.hasNext()) {
        Entry<TickerSpec, CallbackDef> next = listenerIterator.next();
        TickerSpec spec = next.getKey();
        if (next.getValue().equals(callbackDef) && !targetSet.contains(spec)) {
          listenerIterator.remove();
          if (!listeners.containsKey(spec)) {
            byExchange.remove(spec.exchange(), spec);
            updated = true;
          }
        }
      }

      // And add the new ones
      ImmutableListMultimap<String, TickerSpec> targetByExchange = Multimaps.index(targetTickers, TickerSpec::exchange);
      targetTickers.forEach(spec -> listeners.put(spec, callbackDef));
      if (byExchange.putAll(targetByExchange)) {
        updated = true;
      }

      if (updated) {
        updateSubscriptions();
      }
    });
  }

  private void updateSubscriptions() {
    marketDataSubscriptionManager.updateSubscriptions(
        Multimaps.transformValues(
            byExchange,
            s -> MarketDataSubscription.create(s, EnumSet.of(MarketDataType.TICKER))
        )
    );
  }

  @Subscribe
  public void tickerEvent(TickerEvent tickerEvent) {
    LOGGER.debug("Ticker event: {}", tickerEvent);
    long stamp = rwLock.readLock();
    try {
      listeners.get(tickerEvent.spec())
        .forEach(c -> executorService.execute(() -> c.process(tickerEvent)));
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
final class CallbackDef {

  private final String id;
  private final Consumer<TickerEvent> callback;
  private final Lock lock = new ReentrantLock();

  CallbackDef(String id, Consumer<TickerEvent> callback) {
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
    CallbackDef other = (CallbackDef) obj;
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

  void process(TickerEvent tickerEvent) {
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