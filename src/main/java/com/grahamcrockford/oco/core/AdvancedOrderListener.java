package com.grahamcrockford.oco.core;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import com.grahamcrockford.oco.api.AdvancedOrder;
import com.grahamcrockford.oco.api.AdvancedOrderProcessor;
import jersey.repackaged.com.google.common.collect.Maps;
import jersey.repackaged.com.google.common.collect.Sets;

/**
 * Monitors the queue.
 */
@Singleton
public class AdvancedOrderListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedOrderListener.class);

  private final ExchangeService exchangeService;
  private final Injector injector;
  private final TelegramService telegramService;
  private final Set<Long> deadJobIds = Sets.newSetFromMap(Maps.newConcurrentMap());


  @Inject
  AdvancedOrderListener(ExchangeService exchangeService, Injector injector, TelegramService telegramService, ObjectMapper objectMapper) {
    this.exchangeService = exchangeService;
    this.injector = injector;
    this.telegramService = telegramService;
  }

  /**
   * @see com.kjetland.dropwizard.activemq.ActiveMQReceiver#receive(java.lang.Object)
   */
  public void receive(AdvancedOrder job) {
    if (deadJobIds.contains(job.id())) {
      LOGGER.info("Deleted job {}", job.id());
      return;
    }
    process(job);
  }

  /**
   * Adds a job to an in-memory list of jobs which will be read, acknowledged
   * and ignored when next polled, thus removing them from the active list.
   *
   * @param jobId The job ID.
   */
  public void delete(long jobId) {
    deadJobIds.add(jobId);
  }

  private <T extends AdvancedOrder> void process(T order) {
    try {
      @SuppressWarnings("unchecked")
      final Class<? extends AdvancedOrderProcessor<T>> processor = (Class<? extends AdvancedOrderProcessor<T>>) order.processor();
      try {
        final Ticker ticker = exchangeService
          .get(order.basic().exchange())
          .getMarketDataService()
          .getTicker(order.basic().currencyPair());
        injector.getInstance(processor).tick(order, ticker);
        return;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } catch (Throwable e) {
      LOGGER.error("Failed to handle job #" + order.id(), e);
      telegramService.sendMessage("Error handling job #" + order.id() + ": " + e.getMessage());
      throw e;
    }
  }
}