package com.gruelbox.orko.mq;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

public class JobRouteFactory {

  private static final String QUEUE_NAME = "JOB_DURABLE";

  public Route createOn(Channel channel) throws IOException {

    channel.queueDeclare(QUEUE_NAME, true, false, false, null);

    return new Route() {
      @Override
      public void send(byte[] bytes) throws IOException {
        channel.basicPublish("", QUEUE_NAME, null, bytes);
      }

      @Override
      public void consume(Consumer consumer) throws IOException {
        channel.basicConsume(QUEUE_NAME, false, consumer);
      }
    };
  }


  public interface Route {
    public void send(byte[] bytes) throws IOException;
    public void consume(Consumer consumer) throws IOException;
  }
}