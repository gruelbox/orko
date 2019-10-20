package com.gruelbox.orko.marketdata;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.knowm.xchange.utils.ObjectMapperHelper;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.remote.ConsumerSource;
import com.gruelbox.orko.remote.ProducerSource;
import com.gruelbox.orko.remote.Topics;

@Singleton
class RemoteMarketDataSource extends AbstractMarketSourceManager {

  private final ConsumerSource consumerSource;
  private final ProducerSource producerSource;

  private Set<MarketDataSubscription> nextSubs;
  private Map<String, MarketDataSubscription> subsByTopic = Map.of();
  private int phase;

  @Inject
  RemoteMarketDataSource(ConsumerSource consumerSource,
                         ProducerSource producerSource,
                         OrkoConfiguration configuration) {
    super(configuration);
    this.consumerSource = consumerSource;
    this.producerSource = producerSource;
  }

  @Override
  public synchronized void updateSubscriptions(Set<MarketDataSubscription> subscriptions) {
    this.nextSubs = subscriptions;
    wake();
  }

  private void resubscribe(Producer<String, String> producer, Consumer<String, String> consumer) {
    this.subsByTopic = Maps.uniqueIndex(nextSubs, sub -> sub.key().replace('/', '_'));
    if (!nextSubs.isEmpty()) {
      nextSubs.forEach(sub -> {
        logger.info("Sending subscription for {}", sub);
        producer.send(
            new ProducerRecord<String, String>(
                Topics.MARKET_DATA_SUBSCRIBE,
                ObjectMapperHelper.toCompactJSON(sub)));
      });
    }
    consumer.subscribe(subsByTopic.keySet());
    nextSubs = null;
  }

  @Override
  protected void doRun() throws InterruptedException {
    try (var producer = producerSource.get();
         var consumer = consumerSource.get()) {
      while (!Thread.currentThread().isInterrupted() && !isTerminated()) {

        // Before we check for the presence of polls, determine which phase
        // we are going to wait for if there's no work to do - i.e. the
        // next wakeup.
        phase = getPhase();
        if (phase == -1)
          break;

        if (nextSubs != null) {
          resubscribe(producer, consumer);
        }

        if (subsByTopic.isEmpty()) {
          suspend("main", phase, false);
        }

        if (subsByTopic.isEmpty()) {
          continue;
        }

        consumer.poll(Duration.ofSeconds(1))
            .forEach(record -> {
              try {
                MarketDataSubscription sub = subsByTopic.get(record.topic());
                if (sub == null) {
                  logger.warn("Unexpected topic: {} - of expected: {}", record.topic(), subsByTopic.keySet());
                } else {
                  switch (sub.type()) {
                    case BALANCE:
                      // TODO
                      break;
                    case OPEN_ORDERS:
                      openOrdersOut.emit(ObjectMapperHelper.readValue(record.value(), OpenOrdersEvent.class));
                      break;
                    case ORDER:
                      orderStatusChangeOut.emit(ObjectMapperHelper.readValue(record.value(), OrderChangeEvent.class));
                      break;
                    case ORDERBOOK:
                      orderbookOut.emit(ObjectMapperHelper.readValue(record.value(), OrderBookEvent.class));
                      break;
                    case TICKER:
                      tickersOut.emit(ObjectMapperHelper.readValue(record.value(), TickerEvent.class));
                      break;
                    case TRADES:
                      tradesOut.emit(ObjectMapperHelper.readValue(record.value(), TradeEvent.class));
                      break;
                    case USER_TRADE:
                      userTradesOut.emit(ObjectMapperHelper.readValue(record.value(), UserTradeEvent.class));
                      break;
                    default:
                      logger.warn("Unknown message type: {}", record);
                      break;
                  }
                }
              } catch (Exception e) {
                logger.error("Failed to process message : {}", record, e);
              }
            });
      }
    }
  }
}