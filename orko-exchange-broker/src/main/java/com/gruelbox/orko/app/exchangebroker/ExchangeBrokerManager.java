package com.gruelbox.orko.app.exchangebroker;

import static com.gruelbox.orko.marketdata.MarketDataType.OPEN_ORDERS;
import static com.gruelbox.orko.marketdata.MarketDataType.ORDER;
import static com.gruelbox.orko.marketdata.MarketDataType.ORDERBOOK;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static com.gruelbox.orko.marketdata.MarketDataType.TRADES;
import static com.gruelbox.orko.marketdata.MarketDataType.USER_TRADE;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.knowm.xchange.utils.ObjectMapperHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.marketdata.MarketDataSource;
import com.gruelbox.orko.marketdata.MarketDataSubscription;
import com.gruelbox.orko.marketdata.MarketDataSubscriptionManager;
import com.gruelbox.orko.remote.ConsumerSource;
import com.gruelbox.orko.remote.ProducerSource;
import com.gruelbox.orko.remote.Topics;

import io.reactivex.disposables.Disposable;

@Singleton
class ExchangeBrokerManager extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeBrokerManager.class);

  private final MarketDataSource marketDataSubscriptionManager;
  private final OrkoConfiguration orkoConfiguration;
  private final ProducerSource producerSource;
  private final ConsumerSource consumerSource;

  @Inject
  ExchangeBrokerManager(MarketDataSource marketDataSubscriptionManager,
      OrkoConfiguration orkoConfiguration, ProducerSource producerSource, ConsumerSource consumerSource) {
    super();
    this.marketDataSubscriptionManager = marketDataSubscriptionManager;
    this.orkoConfiguration = orkoConfiguration;
    this.producerSource = producerSource;
    this.consumerSource = consumerSource;
  }

  private String subToTopic(MarketDataSubscription sub) {
    return sub.key().replace('/', '_');
  }

  @Override
  protected void run() {
    Thread.currentThread().setName(MarketDataSubscriptionManager.class.getSimpleName());
    LOGGER.info("{} started", this);

    try (Producer<String, String> producer = producerSource.get();
         Consumer<String, String> consumer = consumerSource.get()) {

      consumer.subscribe(List.of(Topics.MARKET_DATA_SUBSCRIBE));
      Map<MarketDataSubscription, Long> subs = new HashMap<>();

      Disposable tickers = marketDataSubscriptionManager.getTickers().subscribe(obj -> {
        producer.send(
            new ProducerRecord<String, String>(
                subToTopic(MarketDataSubscription.create(obj.spec(), TICKER)),
                ObjectMapperHelper.toJSON(obj)));
      });

      Disposable trades = marketDataSubscriptionManager.getTrades().subscribe(obj -> {
        producer.send(
            new ProducerRecord<String, String>(
                subToTopic(MarketDataSubscription.create(obj.spec(), TRADES)),
                ObjectMapperHelper.toJSON(obj)));
      });

      Disposable userTrades = marketDataSubscriptionManager.getUserTrades().subscribe(obj -> {
        producer.send(
            new ProducerRecord<String, String>(
                subToTopic(MarketDataSubscription.create(obj.spec(), USER_TRADE)),
                ObjectMapperHelper.toJSON(obj)));
      });

      Disposable orderBookSnapshots = marketDataSubscriptionManager.getOrderBookSnapshots().subscribe(obj -> {
        producer.send(
            new ProducerRecord<String, String>(
                subToTopic(MarketDataSubscription.create(obj.spec(), ORDERBOOK)),
                ObjectMapperHelper.toJSON(obj)));
      });

      Disposable orderChanges = marketDataSubscriptionManager.getOrderChanges().subscribe(obj -> {
        producer.send(
            new ProducerRecord<String, String>(
                subToTopic(MarketDataSubscription.create(obj.spec(), ORDER)),
                ObjectMapperHelper.toJSON(obj)));
      });

      Disposable orderSnapshots = marketDataSubscriptionManager.getOrderSnapshots().subscribe(obj -> {
        producer.send(
            new ProducerRecord<String, String>(
                subToTopic(MarketDataSubscription.create(obj.spec(), OPEN_ORDERS)),
                ObjectMapperHelper.toJSON(obj)));
      });

      // TODO
  //    Disposable balances = marketDataSubscriptionManager.getBalances().subscribe(obj -> {
  //      producerSource.get().send(
  //          new ProducerRecord<String, String>(
  //              obj.exchange() + "/" + obj.currency() + BALANCE,
  //              ObjectMapperHelper.toJSON(obj)));
  //    });

      try {

        while (true) {

          if (Thread.interrupted()) {
            LOGGER.info("{} stopping due to interrupt", this);
            Thread.currentThread().interrupt();
            break;
          }

          AtomicBoolean modified = new AtomicBoolean();
          long now = System.currentTimeMillis();

          LOGGER.debug("Poll at {}", now);

          consumer.poll(Duration.ofSeconds(orkoConfiguration.getLoopSeconds()))
              .forEach(record -> {
                try {
                  MarketDataSubscription sub = ObjectMapperHelper.readValue(record.value(), MarketDataSubscription.class);
                  if (subs.put(sub, now) == null) {
                    modified.set(true);
                  }
                } catch (Exception e) {
                  LOGGER.error("Failed to process command: {}", record, e);
                }
              });

          LOGGER.debug("Cleanup at {}", now);

          var iterator = subs.entrySet().iterator();
          while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue() < now) {
              LOGGER.debug("Cleaning up {}", entry.getKey());
              iterator.remove();
              modified.set(true);
            }
          }

          if (modified.get()) {
            LOGGER.debug("Resubscribing to {}", subs);
            marketDataSubscriptionManager.updateSubscriptions(subs.keySet());
          }
        }
      } catch (Exception e) {
        LOGGER.error("{} stopping due to uncaught exception", this, e);
      } finally {
        tickers.dispose();
        trades.dispose();
        userTrades.dispose();
        orderBookSnapshots.dispose();
        orderChanges.dispose();
        orderSnapshots.dispose();
        //balances.dispose();
      }
    } finally {
      marketDataSubscriptionManager.updateSubscriptions(Collections.emptySet());
      LOGGER.info("{} stopped", this);
    }
  }
}