package com.grahamcrockford.oco.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.inject.Injector;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.api.AdvancedOrder;
import com.grahamcrockford.oco.api.AdvancedOrderProcessor;
import com.grahamcrockford.oco.db.AdvancedOrderPersistenceService;

@Singleton
class GameLoop extends AbstractScheduledService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GameLoop.class);

  private final OcoConfiguration configuration;
  private final AdvancedOrderPersistenceService persistenceService;
  private final ExchangeService exchangeService;
  private final Injector injector;
  private final TelegramService telegramService;
  private final Map<Long, Integer> jobBackoffLevels = new HashMap<>();
  private final Map<Long, Integer> jobBackoffCountdown = new HashMap<>();


  @Inject
  GameLoop(OcoConfiguration configuration, AdvancedOrderPersistenceService persistenceService, ExchangeService exchangeService, Injector injector, TelegramService telegramService) {
    this.configuration = configuration;
    this.persistenceService = persistenceService;
    this.exchangeService = exchangeService;
    this.injector = injector;
    this.telegramService = telegramService;
  }

  @Override
  protected Scheduler scheduler() {
    return AbstractScheduledService.Scheduler.newFixedRateSchedule(10, configuration.getLoopSeconds(), TimeUnit.SECONDS);
  }

  @Override
  protected void startUp() throws Exception {
    super.startUp();
    LOGGER.info("Started. Will execute every {} seconds", configuration.getLoopSeconds());
  }

  /**
   * Huge concurrency and locking issues here. TODO
   */
  @Override
  protected void runOneIteration() throws Exception {
    try {
      final Iterator<? extends AdvancedOrder> iterator = persistenceService.listJobs().iterator();
      while (iterator.hasNext()) {
        final AdvancedOrder order = iterator.next();
        try {

          Integer backoffCountdown = jobBackoffCountdown.get(order.id());
          if (backoffCountdown == null) {
            process(order);
            jobBackoffLevels.remove(order.id());
            jobBackoffCountdown.remove(order.id());
          } else if (backoffCountdown == 0) {
            process(order);
            jobBackoffLevels.remove(order.id());
            jobBackoffCountdown.remove(order.id());
          } else {
            jobBackoffCountdown.put(order.id(), backoffCountdown - 1);
          }

        } catch (final Throwable e) {

          LOGGER.error("Failed to handle job #" + order.id(), e);
          telegramService.sendMessage("Error handling job #" + order.id() + ": " + e.getMessage());

          Integer backoffLevel = jobBackoffLevels.get(order.id());
          if (backoffLevel == null) {
            backoffLevel = 1;
            jobBackoffLevels.put(order.id(), backoffLevel);
          } else if (backoffLevel == 16) {
            // That's where we stop
          } else {
            backoffLevel = backoffLevel * 2;
            jobBackoffLevels.put(order.id(), backoffLevel);
          }
          jobBackoffCountdown.put(order.id(), backoffLevel);

        }
      }
    } catch (final Throwable e) {
      LOGGER.error("Game loop failed", e);
      telegramService.sendMessage("Game loop failed: " + e.getMessage());
    }
  }

  private <T extends AdvancedOrder> void process(T order) throws Exception {
    @SuppressWarnings("unchecked")
    final Class<? extends AdvancedOrderProcessor<T>> processor = (Class<? extends AdvancedOrderProcessor<T>>) order.processor();
    try {
      final Ticker ticker = exchangeService
        .get(order.basic().exchange())
        .getMarketDataService()
        .getTicker(order.basic().currencyPair());
      injector.getInstance(processor).tick(order, ticker);
      return;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  protected void shutDown() throws Exception {
    LOGGER.info("Shutting down");
    super.shutDown();
  }
}