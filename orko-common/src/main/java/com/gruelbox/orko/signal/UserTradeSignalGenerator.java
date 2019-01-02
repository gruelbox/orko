package com.gruelbox.orko.signal;

/*-
 * ===============================================================================L
 * Orko Common
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.knowm.xchange.dto.trade.UserTrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.marketdata.TradeEvent;
import com.gruelbox.orko.marketdata.TradeHistoryEvent;
import com.gruelbox.orko.spi.TickerSpec;

import io.dropwizard.lifecycle.Managed;

@Singleton
class UserTradeSignalGenerator implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserTradeSignalGenerator.class);

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
   * @param tradeEvent The trade event.
   */
  @Subscribe
  void onTrade(TradeEvent tradeEvent) {
    if (tradeEvent.trade() instanceof UserTrade && cache(tradeEvent.trade().getId())) {
      LOGGER.info("Got streamed user trade: " + tradeEvent);
      eventBus.post(UserTradeEvent.create(tradeEvent.spec(), (UserTrade) tradeEvent.trade()));
    }
  }

  /**
   * For exchanges without streaming support for {@link UserTrade}s, or in
   * case we miss them for any reason, checks the trade history for new
   * trades and reposts them.
   *
   * @param tradeHistoryEvent Trade history event.
   */
  @Subscribe
  void onTradeHistory(TradeHistoryEvent tradeHistoryEvent) {

    List<UserTradeEvent> toPost = new ArrayList<>(tradeHistoryEvent.trades().size());

    latestTimestamps.compute(tradeHistoryEvent.spec(), (tickerSpec, mostRecentPublishTime) -> {

      if (mostRecentPublishTime == null)
        mostRecentPublishTime = startTime;

      Instant mostRecentInBatch = mostRecentPublishTime;
      for (UserTrade userTrade : tradeHistoryEvent.trades()) {
        Instant instant = userTrade.getTimestamp().toInstant();
        if (instant.isAfter(mostRecentPublishTime)) {
          if (cache(userTrade.getId())) {
            LOGGER.info("Got polled user trade: " + userTrade);
            toPost.add(UserTradeEvent.create(tradeHistoryEvent.spec(), userTrade));
          }
          if (mostRecentInBatch == null || instant.isAfter(mostRecentInBatch)) {
            mostRecentInBatch = instant;
          }
        }
      }

      return mostRecentInBatch;
    });

    toPost.forEach(eventBus::post);
  }

  private boolean cache(String id) {
    boolean result = recentTradeIds.asMap().putIfAbsent(id, Boolean.TRUE) == null;
    if (result) {
      LOGGER.info("Skipped reporting trade {} as already reported");
    }
    return result;
  }
}
