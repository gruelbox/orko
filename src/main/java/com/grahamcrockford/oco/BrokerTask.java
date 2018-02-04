package com.grahamcrockford.oco;

import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.dropwizard.lifecycle.Managed;

@Singleton
final class BrokerTask implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(BrokerTask.class);

  private final BrokerService broker;

  @Inject
  BrokerTask() {
    broker = new BrokerService();
  }

  @Override
  public void start() throws Exception {
    LOGGER.info("Starting broker");
    try {
      broker.addConnector("tcp://localhost:61616");
      broker.setPersistent(true);
      broker.setUseJmx(false);
      broker.setBrokerName("localhost");
      broker.setSchedulerSupport(true);
      broker.start();
    } catch (Exception e) {
      LOGGER.error("Failed to start broker", e);
      throw new RuntimeException(e);
    }
    LOGGER.info("Broker started");
  }

  @Override
  public void stop() throws Exception {
    broker.stop();
  }
}