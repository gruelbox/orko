package com.grahamcrockford.oco.core;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.mq.TickerRouteFactory;
import com.grahamcrockford.oco.api.mq.TickerRouteFactory.Route;
import com.grahamcrockford.oco.api.util.Sleep;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Singleton
public class MqTickerSender extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqTickerSender.class);

  private final BlockingQueue<Optional<TickerEvent>> queue = new ArrayBlockingQueue<>(50);

  private final ExchangeEventBus exchangeEventBus;
  private final ConnectionFactory connectionFactory;
  private final Sleep sleep;
  private final ObjectMapper objectMapper;
  private final TickerRouteFactory tickerRouteFactory;


  @Inject
  MqTickerSender(ExchangeEventBus exchangeEventBus,
                 ConnectionFactory connectionFactory,
                 Sleep sleep,
                 ObjectMapper objectMapper,
                 TickerRouteFactory tickerRouteFactory) {
    this.exchangeEventBus = exchangeEventBus;
    this.connectionFactory = connectionFactory;
    this.sleep = sleep;
    this.objectMapper = objectMapper;
    this.tickerRouteFactory = tickerRouteFactory;
  }


  @Override
  protected void run() throws Exception {
    LOGGER.info("{} starting...", this);
    boolean success = false;
    while (!success) {
      try {
        LOGGER.info("{} connecting to MQ", this);
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
          Route route = tickerRouteFactory.createOn(channel);

          LOGGER.info("{} started", this);
          success = true;
          pollQueueLoop(route);
        }
      } catch (InterruptedException e) {
        LOGGER.info("{} interrupted");
        success = true;
        break;
      } catch (Exception e) {
        LOGGER.error(this + " failed. Attemptiing reconnect...", e);
        sleep.sleep();
      }
    }
    LOGGER.info("{} stopping...", this);
  }


  private void pollQueueLoop(Route route) throws IOException, JsonProcessingException, InterruptedException {
    while (isRunning()) {
      try {

        Optional<TickerEvent> event = queue.take();
        if (!event.isPresent())
          break;

        route.send(
          objectMapper.writeValueAsBytes(event),
          routingKey(event.get().spec())
        );

      } catch (Exception e) {
        LOGGER.error(this + " queue poll failed.", e);
        sleep.sleep();
      }
    }
  }


  private String routingKey(TickerSpec spec) {
    return spec.exchange() + "-" + spec.counter() + "-" + spec.base();
  }


  public Subscription enable(TickerSpec spec) {
    return new Subscription() {

      private final String uuid = UUID.randomUUID().toString();

      {
        exchangeEventBus.registerTicker(spec, uuid, this::onTick);
      }

      public void onTick(Ticker ticker) {

      }

      @Override
      public void close() {
        exchangeEventBus.unregisterTicker(spec, uuid);
      }
    };
  }


  @Override
  protected void shutDown() throws Exception {
    queue.put(Optional.empty());
    super.shutDown();
  }


  public interface Subscription extends AutoCloseable {
    @Override
    void close();
  }
}