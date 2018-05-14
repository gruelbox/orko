package com.grahamcrockford.oco.marketdata;

import static com.grahamcrockford.oco.marketdata.MarketDataType.OPEN_ORDERS;
import static com.grahamcrockford.oco.marketdata.MarketDataType.TICKER;

import java.util.Collection;
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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.spi.TickerSpec;


@Singleton
class ExchangeEventBus implements ExchangeEventRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeEventBus.class);

  private final ExecutorService executorService;

  private final Multimap<String, TickerSpec> subscriptionsByExchange = MultimapBuilder.hashKeys().hashSetValues().build();

  private final Multimap<TickerSpec, CallbackDef<TickerEvent>> tickerListeners = MultimapBuilder.hashKeys().hashSetValues().build();
  private final Multimap<TickerSpec, CallbackDef<OpenOrdersEvent>> openOrdersListeners = MultimapBuilder.hashKeys().hashSetValues().build();

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
      if (tickerListeners.put(spec, new CallbackDef<TickerEvent>(subscriberId, callback))) {
        if (subscriptionsByExchange.put(spec.exchange(), spec)) {
          updateSubscriptions();
        }
      }
    });
  }

  @Override
  public void unregisterTicker(TickerSpec spec, String subscriberId) {
    withWriteLock(() -> {
      if (tickerListeners.remove(spec, new CallbackDef<TickerEvent>(subscriberId, null)) && !tickerListeners.containsKey(spec)) {
        subscriptionsByExchange.remove(spec.exchange(), spec);
        updateSubscriptions();
      }
    });
  }

  @Override
  public void changeSubscriptions(Multimap<TickerSpec, MarketDataType> targetSubscriptions,
                                  String subscriberId,
                                  Consumer<TickerEvent> tickerCallback,
                                  Consumer<OpenOrdersEvent> openOrdersCallback) {

    LOGGER.info("Changing subscriptions for subscriber {} to {}", subscriberId, targetSubscriptions);

    withWriteLock(() -> {

      // Remove the subscriptions we don't need
      SetView<TickerSpec> tickersWithDeletions = Sets.union(
        clearUnusedSubscriptionsForSubscriber(targetSubscriptions, subscriberId, tickerListeners),
        clearUnusedSubscriptionsForSubscriber(targetSubscriptions, subscriberId, openOrdersListeners)
      );

      // Disconnect the exchanges we don't need
      boolean updated = false;
      for (TickerSpec spec : tickersWithDeletions) {
        if (!tickerListeners.containsKey(spec) && !openOrdersListeners.containsKey(spec)) {
          subscriptionsByExchange.remove(spec.exchange(), spec);
          updated = true;
        }
      }

      // And add the new ones
      ImmutableListMultimap<String, TickerSpec> targetByExchange = Multimaps.index(targetSubscriptions.keySet(), TickerSpec::exchange);
      targetSubscriptions.asMap().entrySet().forEach(entry -> {
        TickerSpec spec = entry.getKey();
        Collection<MarketDataType> types = entry.getValue();
        types.forEach(type -> {
          if (type == TICKER) {
            tickerListeners.put(spec, new CallbackDef<TickerEvent>(subscriberId, tickerCallback));
          } else if (type == OPEN_ORDERS) {
            openOrdersListeners.put(spec, new CallbackDef<OpenOrdersEvent>(subscriberId, openOrdersCallback));
          }
        });
      });

      if (subscriptionsByExchange.putAll(targetByExchange)) {
        updated = true;
      }

      if (updated) {
        updateSubscriptions();
      }
    });
  }

  private <T> Set<TickerSpec> clearUnusedSubscriptionsForSubscriber(Multimap<TickerSpec, MarketDataType> targetSubscriptions,
                                                                    String subscriberId,
                                                                    Multimap<TickerSpec, CallbackDef<T>> listeners) {
    Builder<TickerSpec> affectedTickers = ImmutableSet.builder();
    CallbackDef<T> subscriberCallback = new CallbackDef<T>(subscriberId, null);
    Iterator<Entry<TickerSpec, CallbackDef<T>>> listenerIterator = listeners.entries().iterator();
    while (listenerIterator.hasNext()) {
      Entry<TickerSpec, CallbackDef<T>> next = listenerIterator.next();
      TickerSpec spec = next.getKey();
      if (next.getValue().equals(subscriberCallback) && !targetSubscriptions.containsEntry(spec, TICKER)) {
        listenerIterator.remove();
        affectedTickers.add(spec);
      }
    }
    return affectedTickers.build();
  }

  private void updateSubscriptions() {
    marketDataSubscriptionManager.updateSubscriptions(
      Multimaps.transformValues(
        subscriptionsByExchange,
        s -> {
          EnumSet<MarketDataType> dataTypes = EnumSet.noneOf(MarketDataType.class);
          if (tickerListeners.containsKey(s)) {
            dataTypes.add(TICKER);
          }
          if (openOrdersListeners.containsKey(s)) {
            dataTypes.add(OPEN_ORDERS);
          }
          return MarketDataSubscription.create(s, dataTypes);
        }
      )
    );
  }

  @Subscribe
  public void tickerEvent(TickerEvent tickerEvent) {
    LOGGER.debug("Ticker event: {}", tickerEvent);
    long stamp = rwLock.readLock();
    try {
      tickerListeners.get(tickerEvent.spec())
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
      openOrdersListeners.get(openOrdersEvent.spec())
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