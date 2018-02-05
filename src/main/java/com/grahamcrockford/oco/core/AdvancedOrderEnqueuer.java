package com.grahamcrockford.oco.core;

import javax.jms.TextMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.api.AdvancedOrder;
import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import com.kjetland.dropwizard.activemq.ActiveMQSender;

/**
 * Simple wrapper for sending orders to the queue.
 */
@Singleton
public class AdvancedOrderEnqueuer {

  private final ActiveMQSender sender;
  private final ObjectMapper objectMapper;
  private final OcoConfiguration ocoConfiguration;

  @Inject
  AdvancedOrderEnqueuer(ActiveMQBundle activeMQBundle, ObjectMapper objectMapper, OcoConfiguration ocoConfiguration) {
    this.objectMapper = objectMapper;
    this.ocoConfiguration = ocoConfiguration;
    this.sender = activeMQBundle.createSender(AdvancedOrder.class.getName(), true);
  }

  /**
   * Enqueues the order for immediate action.
   *
   * @param order The order.
   */
  public void enqueue(AdvancedOrder order) {
    sender.send(order);
  }

  /**
   * Enqueues the order for processing after a minimum of a configured number of milliseconds.
   *
   * @param order The order.
   */
  public void enqueueAfterConfiguredDelay(AdvancedOrder order) {
    enqueueAfterMilliseconds(order, ocoConfiguration.getLoopSeconds() * 1000);
  }

  /**
   * Enqueues the order for processing after a minimum of a certain number of milliseconds.
   *
   * @param order The order.
   * @param milliseconds The time to wait.
   */
  public void enqueueAfterMilliseconds(AdvancedOrder order, long milliseconds) {
    sender.send(session -> {
      try {
        TextMessage textMessage = session.createTextMessage(objectMapper.writeValueAsString(order));
        String correlationId = ActiveMQBundle.correlationID.get();
        if (textMessage.getJMSCorrelationID() == null && correlationId != null) {
          textMessage.setJMSCorrelationID(correlationId);
        }
        textMessage.setLongProperty("AMQ_SCHEDULED_DELAY", milliseconds);
        return textMessage;
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    });
  }
}