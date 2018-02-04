package com.grahamcrockford.oco.db;

import java.util.function.Consumer;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.grahamcrockford.oco.OcoConfiguration;

/**
 * A not particularly pretty wrapper for JMS access.
 *
 * @param <T>
 */
public class QueueAccess<T> implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueueAccess.class);

  private final JsonSerializer<T> jsonSerializer;
  private final OcoConfiguration configuration;
  private final Connection connection;
  private final String queueName;

  QueueAccess(ObjectMapper objectMapper, Connection connection, OcoConfiguration configuration, TypeLiteral<T> typeLiteral) {
    this.connection = connection;
    this.configuration = configuration;
    this.jsonSerializer = new JsonSerializer<>(objectMapper, typeLiteral.getRawType());
    this.queueName = typeLiteral.getRawType().getName();
  }


  public void submit(T item, String correlationID) {
    submit(item, correlationID, 0);
  }

  public void submit(T item, String correlationID, long delayMillis) {
    inSession(session -> {
      try {
        Destination queue = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(queue);
        try {
          TextMessage message = session.createTextMessage(jsonSerializer.serialize(item));
          message.setJMSCorrelationID(correlationID);
          if (delayMillis != 0) {
            if (!ActiveMQTextMessage.class.isInstance(message))
              throw new IllegalArgumentException("Can't delay message on anything except ActiveMQ (I don't know how)");
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delayMillis);
          }
          producer.send(queue, message);
        } finally {
          producer.close();
        }
      } catch (JMSException e) {
        throw new RuntimeException(e);
      }
    });
  }

  public void poll(Consumer<T> work) {
    inSession(session -> {
      try {
        Destination queue = session.createQueue(queueName);
        MessageConsumer consumer = session.createConsumer(queue);
        try {
          Message message = consumer.receive(configuration.getLoopSeconds() * 1000);
          if (message != null) {
            T item = jsonSerializer.deserialize(((TextMessage)message).getText());
            work.accept(item);
            message.acknowledge();
          }
        } finally {
          consumer.close();
        }
      } catch (JMSException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private void inSession(Consumer<Session> work) {
    try {
      Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      try {
        work.accept(session);
      } finally {
        session.close();
      }
    } catch (JMSException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    try {
      connection.close();
    } catch (JMSException e) {
      throw new RuntimeException(e);
    }
  }


  public static final class Factory<T> {

    private final ConnectionFactory connectionFactory;
    private final OcoConfiguration configuration;
    private final ObjectMapper objectMapper;
    private final TypeLiteral<T> typeLiteral;

    @Inject
    Factory(ObjectMapper objectMapper, ConnectionFactory connectionFactory, OcoConfiguration configuration, TypeLiteral<T> typeLiteral) {
      this.objectMapper = objectMapper;
      this.connectionFactory = connectionFactory;
      this.configuration = configuration;
      this.typeLiteral = typeLiteral;
    }

    public QueueAccess<T> create() {
      try {
        Connection connection = connectionFactory.createConnection();
        try {
          connection.start();
          return new QueueAccess<T>(objectMapper, connection, configuration, typeLiteral);
        } catch (Exception e) {
          try {
            connection.close();
          } catch (JMSException e1) {
            // Nothing we can do
          }
          throw new RuntimeException(e);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}