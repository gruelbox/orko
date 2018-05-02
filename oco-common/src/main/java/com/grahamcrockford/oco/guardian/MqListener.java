package com.grahamcrockford.oco.guardian;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.grahamcrockford.oco.mq.JobRouteFactory;
import com.grahamcrockford.oco.spi.Job;
import com.grahamcrockford.oco.telegram.TelegramService;
import com.grahamcrockford.oco.util.Sleep;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

class MqListener extends AbstractIdleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqListener.class);

  private final ConnectionFactory connectionFactory;
  private final Sleep sleep;
  private final ObjectMapper objectMapper;
  private final JobRunner existingJobSubmitter;
  private final TelegramService telegramService;
  private final JobRouteFactory jobRouteFactory;

  private Connection connection;
  private Channel channel;


  @Inject
  MqListener(ConnectionFactory connectionFactory, Sleep sleep,
             ObjectMapper objectMapper, JobRunner existingJobSubmitter,
             TelegramService telegramService,
             JobRouteFactory jobRouteFactory) {
    this.connectionFactory = connectionFactory;
    this.sleep = sleep;
    this.objectMapper = objectMapper;
    this.existingJobSubmitter = existingJobSubmitter;
    this.telegramService = telegramService;
    this.jobRouteFactory = jobRouteFactory;
  }


  @Override
  protected void startUp() throws Exception {
    // TODO need to spend a lot of time thinking about this logic, compare to
    // best practice etc. Probably need to implement a DQL and redelivery TTL
    // etc.
    LOGGER.info("{} starting...", this);
    boolean success = false;
    while (!success) {
      try {
        LOGGER.info("{} connecting to MQ", this);
        connection = connectionFactory.newConnection();
        channel = connection.createChannel();
        channel.basicQos(10);
        jobRouteFactory.createOn(channel).consume(new DefaultConsumer(channel) {
          @Override
          public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
            handle(envelope, body);
          }
        });
        success = true;
      } catch (IOException e) {
        LOGGER.error(this + " failed to connect. Retrying...", e);
        sleep.sleep();
      }
    }
    LOGGER.info("{} started", this);
  }

  private void handle(Envelope envelope, byte[] body) throws IOException {
    LOGGER.debug("{} received new job. Handling", this);
    Job job;
    try {
      job = objectMapper.readValue(body, Job.class);
    } catch (Exception e)  {
      telegramService.sendMessage("Job serialisation error");
      throw new RuntimeException("Failed to parse message body: " + body);
    }
    LOGGER.info("{} processing job {}", MqListener.this, job.id());
    try {
      existingJobSubmitter.runNew(
        job,
        () -> channel.basicAck(envelope.getDeliveryTag(), false),
        () -> channel.basicReject(envelope.getDeliveryTag(), true)
      );
    } catch (Throwable t) {
      LOGGER.error(this + " job failed: " + job.id(), t);
      telegramService.safeSendMessage(this + " job failed: " + job.id());
    }
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