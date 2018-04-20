package com.grahamcrockford.oco.mq;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

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
    LOGGER.info("Creating connection factory to " + configuration.getClientURI());
    ConnectionFactory connectionFactory = new ConnectionFactory();
    try {
      connectionFactory.setUri(configuration.getClientURI());
    } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
    return connectionFactory;
  }
}