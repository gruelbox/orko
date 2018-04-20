package com.grahamcrockford.oco.ticker;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.exchange.ExchangeService;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.util.Sleep;

import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.ProductSubscription.ProductSubscriptionBuilder;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import io.reactivex.disposables.Disposable;

@Singleton
@VisibleForTesting
public class TickerGenerator extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TickerGenerator.class);

  private final Set<TickerSpec> activePolling = Collections.newSetFromMap(new ConcurrentHashMap<TickerSpec, Boolean>());
  private final Multimap<String, Disposable> subsPerExchange = HashMultimap.create();
  private final Multimap<String, TickerSpec> tickersPerExchange = HashMultimap.create();

  private final EventBus eventBus;
  private final ExchangeService exchangeService;
  private final Sleep sleep;


  @Inject
  @VisibleForTesting
  public TickerGenerator(EventBus eventBus, ExchangeService exchangeService, Sleep sleep) {
    this.exchangeService = exchangeService;
    this.eventBus = eventBus;
    this.sleep = sleep;
  }

  /**
   * Starts handling for a ticker specification.
   *
   * @param spec The specification for the ticker.
   */
  public void start(TickerSpec spec) {
    Exchange exchange = exchangeService.get(spec.exchange());
    boolean streaming = exchange instanceof StreamingExchange;
    if (streaming) {
      subscribe(spec, exchange);
    } else {
      activePolling.add(spec);
      LOGGER.info("Subscribing to ticker poll: " + spec);
    }
  }

  /**
   * If this is the last listening client for the ticker, stops the listener.
   *
   * @param spec The specification for the ticker.
   */
  public void stop(TickerSpec spec) {
    Exchange exchange = exchangeService.get(spec.exchange());
    boolean streaming = exchange instanceof StreamingExchange;
    if (streaming) {
      unsubscribe(spec);
    } else {
      activePolling.remove(spec);
      LOGGER.info("Unsubscribing from ticker poll: " + spec);
    }
  }

  private synchronized void subscribe(TickerSpec spec, Exchange exchange) {
    LOGGER.info("Subscribing to ticker stream: " + spec);

    StreamingExchange streamingExchange = (StreamingExchange)exchange;

    // Remove all the old subscriptions and disconnect
    unsubscribeAll(streamingExchange, spec.exchange());

    // Add our new ticker
    tickersPerExchange.put(spec.exchange(), spec);

    resubscribeAll(streamingExchange, tickersPerExchange.get(spec.exchange()));
    LOGGER.info("Subscribed to ticker stream: " + spec);
  }

  private void unsubscribeAll(StreamingExchange streamingExchange, String exchange) {
    Collection<Disposable> oldSubs = subsPerExchange.get(exchange);
    if (!oldSubs.isEmpty()) {
      LOGGER.info("Disconnecting from exchange: " + exchange);
      streamingExchange.disconnect().blockingAwait(); // Seems odd but this avoids an NPE when unsubscribing

      if (!oldSubs.isEmpty()) {
        oldSubs.forEach(Disposable::dispose);
      }
      subsPerExchange.removeAll(exchange);
    }
  }

  private void resubscribeAll(StreamingExchange streamingExchange, Collection<TickerSpec> tickers) {
    if (tickers.isEmpty())
      return;

    // Connect, specifying all the tickers we need
    ProductSubscriptionBuilder builder = ProductSubscription.create();
    tickers.stream().map(TickerSpec::currencyPair).forEach(builder::addTicker);
    streamingExchange.connect(builder.build()).blockingAwait();

    // Add the subscriptions
    StreamingMarketDataService marketDataService = streamingExchange.getStreamingMarketDataService();
    for (TickerSpec s : tickers) {
      Disposable subscription = marketDataService
          .getTicker(s.currencyPair())
          .subscribe(
            ticker -> onTicker(s, ticker),
            throwable -> LOGGER.error("Error in subscribing tickers.", throwable)
          );
      subsPerExchange.put(s.exchange(), subscription);
    }
  }

  private synchronized void unsubscribe(TickerSpec spec) {
    LOGGER.info("Unsubscribing from ticker stream: " + spec);

    Exchange exchange = exchangeService.get(spec.exchange());
    StreamingExchange streamingExchange = (StreamingExchange)exchange;

    // Remove all the old subscriptions and disconnect
    unsubscribeAll(streamingExchange, spec.exchange());

    // Remove our  ticker
    tickersPerExchange.remove(spec.exchange(), spec);

    resubscribeAll(streamingExchange, tickersPerExchange.get(spec.exchange()));
    LOGGER.info("Unsubscribed from ticker stream: " + spec);
  }

  private void onTicker(TickerSpec spec, Ticker ticker) {
    LOGGER.debug("Got ticker {} on {}", ticker, spec);
    eventBus.post(TickerEvent.create(spec, ticker));
  }

  @Override
  protected void run() {
    Thread.currentThread().setName("Ticker generator");
    LOGGER.info(this + " started");
    while (isRunning()) {
      try {
        activePolling.forEach(this::fetchAndBroadcast);
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

  private void fetchAndBroadcast(TickerSpec spec) {
    try {
      Ticker ticker = exchangeService.get(spec.exchange()).getMarketDataService().getTicker(spec.currencyPair());
      onTicker(spec, ticker);
    } catch (Throwable e) {
      LOGGER.error("Failed fetching ticker: " + spec, e);
    }
  }
}