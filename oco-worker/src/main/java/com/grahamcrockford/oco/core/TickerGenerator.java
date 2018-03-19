package com.grahamcrockford.oco.core;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.exchange.ExchangeService;
import com.grahamcrockford.oco.api.util.Sleep;
import com.grahamcrockford.oco.spi.TickerSpec;

@Singleton
class TickerGenerator extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TickerGenerator.class);

  private final Set<TickerSpec> active = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final EventBus eventBus;
  private final ExchangeService exchangeService;
  private final Sleep sleep;

  @Inject
  TickerGenerator(EventBus eventBus, ExchangeService exchangeService, Sleep sleep) {
    this.exchangeService = exchangeService;
    this.eventBus = eventBus;
    this.sleep = sleep;
  }

  public void start(TickerSpec spec) {
    active.add(spec);

  }

  public void stop(TickerSpec spec) {
    active.remove(spec);
  }

  @Override
  protected void run() {
    Thread.currentThread().setName("Ticker generator");
    LOGGER.info(this + " started");
    while (isRunning()) {
      try {
        active.forEach(spec -> {
          try {
            Ticker ticker = exchangeService.get(spec.exchange()).getMarketDataService().getTicker(spec.currencyPair());
            eventBus.post(TickerEvent.create(spec, ticker));
          } catch (Throwable e) {
            LOGGER.error("Failed fetching ticker: " + spec, e);
          }
        });
      } catch (Throwable e) {
        LOGGER.error("Serious error. Trying to stay alive", e);
      }
      try {
        sleep.sleep();
      } catch (InterruptedException e) {
        break;
      }
    }
    LOGGER.info(this + " stopped");
  }
}