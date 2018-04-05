package com.grahamcrockford.oco.api.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.OcoConfiguration;
import com.rabbitmq.client.ConnectionFactory;

public class MqModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqModule.class);

  @Provides
  MqConfiguration config(OcoConfiguration config) {
    return config.getMq();
  }

  @Provides
  @Singleton
  ConnectionFactory connectionFactory(MqConfiguration configuration) {
    LOGGER.info("Creating connection factory to " + configuration.getHost());
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(configuration.getHost());
    return connectionFactory;
  }
}