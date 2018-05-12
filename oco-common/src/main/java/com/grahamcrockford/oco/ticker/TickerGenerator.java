package com.grahamcrockford.oco.ticker;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
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
import jersey.repackaged.com.google.common.collect.Lists;

@Singleton
@VisibleForTesting
public class TickerGenerator extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TickerGenerator.class);

  private final Set<TickerSpec> activePolling = Collections.newSetFromMap(new ConcurrentHashMap<TickerSpec, Boolean>());
  private final Multimap<String, Disposable> subsPerExchange = HashMultimap.create();
  private final Multimap<String, TickerSpec> subscribedTickersPerExchange = HashMultimap.create();

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
   * Updates the subscriptions for the specified exchanges.
   *
   * @param byExchange The exchanges and subscriptions for each.
   */
  public synchronized void updateSubscriptions(Multimap<String, TickerSpec> byExchange) {
    LOGGER.info("Updating subscriptions to: " + byExchange);

    // Disconnect any streaming exchanges where the tickers currently
    // subscribed mismatch the ones we want.
    Set<String> unchanged = disconnectChangedExchanges(byExchange);

    // Add new subscriptions
    subscribe(byExchange, unchanged);

    // Remove any active polling tickers we're not interested in anymore
    activePolling.removeIf(spec -> !byExchange.values().contains(spec));
  }

  private Set<String> disconnectChangedExchanges(Multimap<String, TickerSpec> byExchange) {
    Builder<String> unchanged = ImmutableSet.builder();

    List<String> changed = Lists.newArrayListWithCapacity(subscribedTickersPerExchange.keySet().size());

    for (Entry<String, Collection<TickerSpec>> entry : subscribedTickersPerExchange.asMap().entrySet()) {

      String exchangeName = entry.getKey();
      Collection<TickerSpec> tickers = entry.getValue();
      Collection<TickerSpec> target = byExchange.get(exchangeName);

      LOGGER.info("Exchange {} has {}, wants {}", exchangeName, tickers, target);

      if (tickers.equals(target)) {
        unchanged.add(exchangeName);
      } else {
        changed.add(exchangeName);
      }
    }

    changed.forEach(exchangeName -> {
      LOGGER.info("... disconnecting " + exchangeName);
      disconnectExchange(exchangeName);
      subsPerExchange.removeAll(exchangeName);
      subscribedTickersPerExchange.removeAll(exchangeName);
    });

    return unchanged.build();
  }

  private void disconnectExchange(String exchangeName) {
    Collection<Disposable> oldSubs = subsPerExchange.get(exchangeName);
    if (!oldSubs.isEmpty()) {
      LOGGER.info("Disconnecting from exchange: " + exchangeName);
      StreamingExchange exchange = (StreamingExchange) exchangeService.get(exchangeName);
      exchange.disconnect().blockingAwait(); // Seems odd but this avoids an NPE when unsubscribing
      if (!oldSubs.isEmpty()) {
        oldSubs.forEach(Disposable::dispose);
      }
      LOGGER.info("Disconnected from exchange: " + exchangeName);
    }
  }

  private void subscribe(Multimap<String, TickerSpec> byExchange, Set<String> unchanged) {
    byExchange.asMap()
      .entrySet()
      .stream()
      .filter(entry -> !unchanged.contains(entry.getKey()))
      .forEach(entry -> {
        Exchange exchange = exchangeService.get(entry.getKey());
        Collection<TickerSpec> tickersForExchange = entry.getValue();
        boolean streaming = exchange instanceof StreamingExchange;
        if (streaming) {
          subscribedTickersPerExchange.putAll(entry.getKey(), tickersForExchange);
          subscribeExchange((StreamingExchange)exchange, tickersForExchange, entry.getKey());
        } else {
          pollExchange(tickersForExchange);
        }
      });
  }

  private void subscribeExchange(StreamingExchange streamingExchange, Collection<TickerSpec> tickers, String exchangeName) {
    if (tickers.isEmpty())
      return;
    LOGGER.info("Connecting to exchange: " + exchangeName);
    openConnections(streamingExchange, tickers);
    LOGGER.info("Connected to exchange: " + exchangeName);
  }

  private void openConnections(StreamingExchange streamingExchange, Collection<TickerSpec> tickers) {
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

  private void pollExchange(Collection<TickerSpec> specs) {
    LOGGER.info("Subscribing to ticker polls: " + specs);
    activePolling.addAll(specs);
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