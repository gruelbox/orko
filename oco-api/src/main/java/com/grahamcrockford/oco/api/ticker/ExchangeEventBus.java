package com.grahamcrockford.oco.api.ticker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;

import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.spi.TickerSpec;

@Singleton
class ExchangeEventBus implements ExchangeEventRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeEventBus.class);

  private final ExecutorService executorService;
  private final Multimap<TickerSpec, CallbackDef> listeners = MultimapBuilder.hashKeys().hashSetValues().build();
  private final StampedLock rwLock = new StampedLock();
  private final TickerGenerator tickerGenerator;

  @Inject
  ExchangeEventBus(EventBus eventBus, ExecutorService executorService, TickerGenerator tickerGenerator) {
    this.executorService = executorService;
    this.tickerGenerator = tickerGenerator;
    eventBus.register(this);
  }

  @Override
  public void registerTicker(TickerSpec spec, String jobId, Consumer<Ticker> callback) {
    long stamp = rwLock.writeLock();
    try {
      if (listeners.put(spec, new CallbackDef(jobId, callback))) {
        tickerGenerator.start(spec);
      }
    } finally {
      rwLock.unlockWrite(stamp);
    }
  }

  @Override
  public void unregisterTicker(TickerSpec spec, String jobId) {
    long stamp = rwLock.writeLock();
    try {
      listeners.remove(spec, new CallbackDef(jobId, null));
      if (!listeners.containsKey(spec))
        tickerGenerator.stop(spec);
    } finally {
      rwLock.unlockWrite(stamp);
    }
  }

  @Subscribe
  public void tickerEvent(TickerEvent tickerEvent) {
    LOGGER.debug("Ticker event: {}", tickerEvent);
    long stamp = rwLock.readLock();
    try {
      listeners.get(tickerEvent.spec()).forEach(c -> executorService.execute(() -> c.process(tickerEvent.ticker())));
    } finally {
      rwLock.unlockRead(stamp);
    }
  }
}

final class CallbackDef {

  private final String id;
  private final Consumer<Ticker> callback;
  private final Lock lock = new ReentrantLock();

  CallbackDef(String id, Consumer<Ticker> callback) {
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

  void process(Ticker ticker) {
    // Prevent passing more tickers to the same job if it's still processing
    // an old one.
    if (!lock.tryLock())
      return;
    try {
      callback.accept(ticker);
    } finally {
      lock.unlock();
    }
  }
}