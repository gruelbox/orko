package com.grahamcrockford.oco.core;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.api.AdvancedOrder;
import com.mongodb.DB;
import com.mongodb.WriteResult;


/**
 * Simple wrapper for sending orders to the queue.
 */
@Singleton
public class AdvancedOrderEnqueuer {

  private final OcoConfiguration ocoConfiguration;
  private final DB database;
  private final ObjectMapper objectMapper;

  @Inject
  AdvancedOrderEnqueuer(DB database, ObjectMapper objectMapper, OcoConfiguration ocoConfiguration) {
    this.database = database;
    this.objectMapper = objectMapper;
    this.ocoConfiguration = ocoConfiguration;
  }

  /**
   * Enqueues the order for immediate action.
   *
   * @param order The order.
   */
  @SuppressWarnings("unchecked")
  public <T extends AdvancedOrder> T enqueue(T order) {
    Jongo jongo = new Jongo(database);
    MongoCollection collection = jongo.getCollection("ADVANCEDORDER");
    WriteResult result = collection.insert(order);

    return null;
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
//    sender.send(session -> {
//      try {
//        TextMessage textMessage = session.createTextMessage(objectMapper.writeValueAsString(order));
//        String correlationId = ActiveMQBundle.correlationID.get();
//        if (textMessage.getJMSCorrelationID() == null && correlationId != null) {
//          textMessage.setJMSCorrelationID(correlationId);
//        }
//        textMessage.setLongProperty("AMQ_SCHEDULED_DELAY", milliseconds);
//        return textMessage;
//      } catch (JsonProcessingException e) {
//        throw new RuntimeException(e);
//      }
//    });
  }
}