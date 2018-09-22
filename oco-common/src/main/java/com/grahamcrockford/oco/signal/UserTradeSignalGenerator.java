package com.grahamcrockford.oco.signal;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.marketdata.Trade;
import com.grahamcrockford.oco.marketdata.TradeEvent;
import com.grahamcrockford.oco.marketdata.TradeHistoryEvent;
import com.grahamcrockford.oco.spi.TickerSpec;

import io.dropwizard.lifecycle.Managed;

@Singleton
class UserTradeSignalGenerator implements Managed {

  private static final Set<String> NATIVELY_SUPPORTED_EXCHANGES = ImmutableSet.of("gdax", "gdax-sandbox");

  private final EventBus eventBus;
  private final ConcurrentMap<TickerSpec, Instant> priorState = new ConcurrentHashMap<>();

  @Inject
  UserTradeSignalGenerator(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public void start() throws Exception {
    eventBus.register(this);
  }

  @Override
  public void stop() throws Exception {
    eventBus.unregister(this);
  }

  @Subscribe
  void onTrade(TradeEvent e) {
    if (e.trade().orderId() != null) {
      eventBus.post(UserTradeEvent.create(e.spec(), e.trade()));
    }
  }

  @Subscribe
  void onTradeHistory(TradeHistoryEvent e) {
    if (NATIVELY_SUPPORTED_EXCHANGES.contains(e.spec().exchange()))
      return;
    priorState.compute(e.spec(), (k, latest) -> {
      Instant mostRecentInBatch = latest;
      for (Trade t : e.trades()) {
        Instant i = t.timestamp().toInstant();
        if (i.isAfter(latest)) {
          eventBus.post(UserTradeEvent.create(e.spec(), t));
          if (i.isAfter(mostRecentInBatch)) {
            mostRecentInBatch = i;
          }
        }
      }
      return mostRecentInBatch;
    });
  }
}