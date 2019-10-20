package com.gruelbox.orko.remote;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.OrkoConfiguration;

import brave.Tracing;
import io.dropwizard.kafka.KafkaConsumerBundle;
import io.dropwizard.setup.Environment;

@Singleton
public class ConsumerSource {

  private final KafkaConsumerBundle<String, String, OrkoConfiguration> consumerBundle;
  private final Environment environment;
  private final OrkoConfiguration configuration;

  @Inject
  ConsumerSource(KafkaConsumerBundle<String, String, OrkoConfiguration> consumerBundle, Environment environment,
      OrkoConfiguration configuration) {
    super();
    this.consumerBundle = consumerBundle;
    this.environment = environment;
    this.configuration = configuration;
  }

  public Consumer<String, String> get() {
    return consumerBundle.getKafkaConsumerFactory(configuration)
        .build(environment.lifecycle(), environment.healthChecks(),
            Tracing.current(), new NoOpConsumerRebalanceListener());
  }
}
