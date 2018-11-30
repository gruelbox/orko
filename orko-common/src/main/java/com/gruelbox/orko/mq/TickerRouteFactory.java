package com.gruelbox.orko.mq;

import java.io.IOException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

public class TickerRouteFactory {

  private static String EXCHANGE_NAME = "TICKER";

  public Route createOn(Channel channel) throws IOException {

    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, false);

    return new Route() {

      String queueName;

      @Override
      public void send(byte[] bytes, String routingKey) throws IOException {
        channel.basicPublish(EXCHANGE_NAME, routingKey, null, bytes);
      }

      @Override
      public void consume(Consumer consumer) throws IOException {
        queueName = channel.queueDeclare().getQueue();
        channel.basicConsume(queueName, false, consumer);
      }

      @Override
      public void subscribe(String routingKey) throws IOException{
        channel.queueBind(queueName, EXCHANGE_NAME, routingKey);
      }
    };
  }

  public interface Route {
    public void send(byte[] bytes, String routingKey) throws IOException;
    public void subscribe(String routingKey) throws IOException;
    public void consume(Consumer consumer) throws IOException;
  }
}