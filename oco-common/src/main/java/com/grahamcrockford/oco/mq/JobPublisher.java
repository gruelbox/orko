package com.grahamcrockford.oco.mq;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.grahamcrockford.oco.mq.JobRouteFactory.Route;
import com.grahamcrockford.oco.spi.Job;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class JobPublisher {

  private final ConnectionFactory connectionFactory;
  private final ObjectMapper objectMapper;
  private final JobRouteFactory jobRouteFactory;

  @Inject
  JobPublisher(ConnectionFactory connectionFactory, ObjectMapper objectMapper, JobRouteFactory jobRouteFactory) {
    this.connectionFactory = connectionFactory;
    this.objectMapper = objectMapper;
    this.jobRouteFactory = jobRouteFactory;
  }

  public void publishJob(Job job) throws PublishFailedException {
    try (Connection connection = connectionFactory.newConnection();
         Channel channel = connection.createChannel()) {

      CountDownLatch wait = new CountDownLatch(1);
      AtomicBoolean success = new AtomicBoolean();

      Route route = jobRouteFactory.createOn(channel);

      channel.confirmSelect();
      channel.addConfirmListener(new ConfirmListener() {
        @Override
        public void handleAck(long seqNo, boolean multiple) {
          success.set(true);
          wait.countDown();
        }
        @Override
        public void handleNack(long seqNo, boolean multiple) {
          wait.countDown();
       }
      });

      route.send(objectMapper.writeValueAsBytes(job));

      wait.await();
      if (!success.get())
        throw new PublishFailedException("Message not delivered");

    } catch (IOException | TimeoutException | InterruptedException e) {
      throw new PublishFailedException(e);
    }
  }


  public static final class PublishFailedException extends Exception {

    private static final long serialVersionUID = 8392693668659024332L;

    PublishFailedException(String message) {
      super(message);
    }

    PublishFailedException(Throwable cause) {
      super(cause);
    }

  }
}