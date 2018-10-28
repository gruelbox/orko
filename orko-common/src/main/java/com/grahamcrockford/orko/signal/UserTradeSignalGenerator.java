package com.grahamcrockford.orko.signal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.knowm.xchange.dto.trade.UserTrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.exchange.Exchanges;
import com.grahamcrockford.orko.marketdata.TradeEvent;
import com.grahamcrockford.orko.marketdata.TradeHistoryEvent;
import com.grahamcrockford.orko.spi.TickerSpec;

import io.dropwizard.lifecycle.Managed;

@Singleton
class UserTradeSignalGenerator implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserTradeSignalGenerator.class);
  private static final Set<String> NATIVELY_SUPPORTED_EXCHANGES = ImmutableSet.of(Exchanges.GDAX, Exchanges.GDAX_SANDBOX, Exchanges.BINANCE);

  private final EventBus eventBus;
  private final ConcurrentMap<TickerSpec, Instant> latestTimestamps = new ConcurrentHashMap<>();
  private final Cache<String, Boolean> recentTradeIds = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();
  private final Instant startTime;

  @Inject
  UserTradeSignalGenerator(EventBus eventBus) {
    this.eventBus = eventBus;
    this.startTime = Instant.now();
  }

  @Override
  public void start() throws Exception {
    eventBus.register(this);
  }

  @Override
  public void stop() throws Exception {
    eventBus.unregister(this);
  }

  /**
   * Filters out {@link UserTrade}s from the trade stream for an exchange
   * and reposts them.  Deduplicates recent trades which might have arrived
   * from {@link #onTradeHistory(TradeHistoryEvent)} in case support is
   * quietly added to an exchange without updating
   * {@link #NATIVELY_SUPPORTED_EXCHANGES}.
   *
   * @param e The trade event.
   */
  @Subscribe
  void onTrade(TradeEvent e) {
    if (e.trade() instanceof UserTrade && cache(e.trade().getId())) {
      LOGGER.info("Got streamed user trade: " + e);
      eventBus.post(UserTradeEvent.create(e.spec(), (UserTrade) e.trade()));
    }
  }

  /**
   * For exchanges without streaming support for {@link UserTrade}s, checks
   * the trade history for new trades and reposts them.
   *
   * @param e Trade history event.
   */
  @Subscribe
  void onTradeHistory(TradeHistoryEvent e) {

    if (NATIVELY_SUPPORTED_EXCHANGES.contains(e.spec().exchange()))
      return;

    List<UserTradeEvent> toPost = new ArrayList<>(e.trades().size());

    latestTimestamps.compute(e.spec(), (k, latest) -> {
      if (latest == null)
        latest = startTime;
      Instant mostRecentInBatch = latest;
      for (UserTrade t : e.trades()) {
        Instant i = t.getTimestamp().toInstant();
        if (i.isAfter(latest)) {
          if (cache(t.getId())) {
            toPost.add(UserTradeEvent.create(e.spec(), t));
          }
          if (mostRecentInBatch == null || i.isAfter(mostRecentInBatch)) {
            mostRecentInBatch = i;
          }
        }
      }
      return mostRecentInBatch;
    });

    toPost.forEach(eventBus::post);
  }

  private boolean cache(String id) {
    return recentTradeIds.asMap().putIfAbsent(id, Boolean.TRUE) == null;
  }
}