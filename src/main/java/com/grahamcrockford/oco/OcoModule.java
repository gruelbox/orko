package com.grahamcrockford.oco;

import javax.jms.ConnectionFactory;
import javax.ws.rs.client.Client;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.pool.PooledConnectionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.oco.core.CoreModule;
import com.grahamcrockford.oco.resources.ResourcesModule;

/**
 * Top level bindings.
 */
class OcoModule extends AbstractModule {

  private final ObjectMapper objectMapper;
  private final OcoConfiguration configuration;
  private final Client client;

  public OcoModule(OcoConfiguration configuration, ObjectMapper objectMapper, Client client) {
    this.configuration = configuration;
    this.objectMapper = objectMapper;
    this.client = client;
  }

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Service.class);
    install(new CoreModule());
    install(new ResourcesModule());
  }

  @Provides
  ObjectMapper objectMapper() {
    return objectMapper;
  }

  @Provides
  OcoConfiguration config() {
    return configuration;
  }

  @Provides
  Client client() {
    return client;
  }

  @Provides
  @Singleton
  ConnectionFactory connectionFactory() {


    ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("failover:vm://localhost?create=false");

    RedeliveryPolicy queuePolicy = new RedeliveryPolicy();
    queuePolicy.setInitialRedeliveryDelay(100);
    queuePolicy.setRedeliveryDelay(200);
    queuePolicy.setUseExponentialBackOff(true);
    queuePolicy.setMaximumRedeliveries(50);

    activeMQConnectionFactory.setRedeliveryPolicy(queuePolicy);

    return new PooledConnectionFactory(activeMQConnectionFactory);
  }
}