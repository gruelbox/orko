package com.grahamcrockford.oco.core;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.grahamcrockford.oco.api.mq.Queue;
import com.grahamcrockford.oco.api.util.Sleep;
import com.grahamcrockford.oco.spi.Job;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

class MqListener extends AbstractIdleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqListener.class);

  private final ConnectionFactory connectionFactory;
  private final Sleep sleep;
  private final ObjectMapper objectMapper;
  private final ExistingJobSubmitter existingJobSubmitter;

  private Connection connection;
  private Channel channel;


  @Inject
  MqListener(ConnectionFactory connectionFactory, Sleep sleep, ObjectMapper objectMapper, ExistingJobSubmitter existingJobSubmitter) {
    this.connectionFactory = connectionFactory;
    this.sleep = sleep;
    this.objectMapper = objectMapper;
    this.existingJobSubmitter = existingJobSubmitter;
  }


  @Override
  protected void startUp() throws Exception {
    LOGGER.info(this + " starting...");
    boolean success = false;
    while (!success) {
      try {
        LOGGER.info("Connecting to MQ...");
        connection = connectionFactory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(Queue.JOB, false, false, false, null);
        com.rabbitmq.client.Consumer consumer = new DefaultConsumer(channel) {
          @Override
          public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
            LOGGER.info(this + " received new job. Handling");
            Job job = objectMapper.readValue(body, Job.class);
            existingJobSubmitter.submitExisting(job);
          }
        };
        channel.basicConsume(Queue.JOB, true, consumer);
        success = true;
      } catch (IOException e) {
        LOGGER.error(this + " failed to connect. Retrying...", e);
        sleep.sleep();
      }
    }
    LOGGER.info(this + " started");
  }

  @Override
  protected void shutDown() throws Exception {
    LOGGER.info(this + " stopping");
    channel.close();
    connection.close();
    LOGGER.info(this + " stopped");
  }

  @Override
  protected String serviceName() {
    return getClass().getSimpleName() + "[" + System.identityHashCode(this) + "]";
  }
}