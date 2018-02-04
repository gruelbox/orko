package com.grahamcrockford.oco.core;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Injector;
import com.grahamcrockford.oco.api.AdvancedOrder;
import com.grahamcrockford.oco.api.AdvancedOrderProcessor;
import com.grahamcrockford.oco.db.QueueAccess;
import com.grahamcrockford.oco.db.QueueAccess.Factory;

/**
 * Monitors the queue.
 */
@Singleton
class JMSListener extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(JMSListener.class);

  private final ExchangeService exchangeService;
  private final Injector injector;
  private final TelegramService telegramService;
  private final Factory<AdvancedOrder> queueAccessFactory;

  private final AtomicBoolean stop = new AtomicBoolean();


  @Inject
  JMSListener(ExchangeService exchangeService, Injector injector, TelegramService telegramService,
           ObjectMapper objectMapper, QueueAccess.Factory<AdvancedOrder> queueAccessFactory) {
    this.exchangeService = exchangeService;
    this.injector = injector;
    this.telegramService = telegramService;
    this.queueAccessFactory = queueAccessFactory;
  }

  @Override
  protected void run() throws Exception {
    LOGGER.info("Started");
    while (!stop.get()) {
      try {
        try (QueueAccess<AdvancedOrder> queueAccess = queueAccessFactory.create()) {
          while (!stop.get()) {
            queueAccess.poll(job -> process(job, queueAccess));
          }
        }
      } catch (final Throwable e) {
        LOGGER.error("Game loop failed", e);
        telegramService.sendMessage("Game loop failed: " + e.getMessage());
      }
    }
    LOGGER.info("Finished");
  }

  private <T extends AdvancedOrder> void process(T order, QueueAccess<AdvancedOrder> queueAccess) {
    try {
      @SuppressWarnings("unchecked")
      final Class<? extends AdvancedOrderProcessor<T>> processor = (Class<? extends AdvancedOrderProcessor<T>>) order.processor();
      try {
        final Ticker ticker = exchangeService
          .get(order.basic().exchange())
          .getMarketDataService()
          .getTicker(order.basic().currencyPair());
        injector.getInstance(processor).tick(order, ticker, queueAccess);
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

  @Override
  protected void shutDown() throws Exception {
    stop.set(true);
    super.shutDown();
  }
}