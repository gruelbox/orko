package com.gruelbox.orko.remote;

import java.util.Collections;

import org.apache.kafka.clients.producer.Producer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.OrkoConfiguration;

import brave.Tracing;
import io.dropwizard.kafka.KafkaProducerBundle;
import io.dropwizard.setup.Environment;

@Singleton
public class ProducerSource {

  private final KafkaProducerBundle<String, String, OrkoConfiguration> producerBundle;
  private final Environment environment;
  private final OrkoConfiguration configuration;

  @Inject
  ProducerSource(KafkaProducerBundle<String, String, OrkoConfiguration> producerBundle, Environment environment,
      OrkoConfiguration configuration) {
    super();
    this.producerBundle = producerBundle;
    this.environment = environment;
    this.configuration = configuration;
  }

  public Producer<String, String> get() {
    return producerBundle.getKafkaProducerFactory(configuration)
        .build(environment.lifecycle(), environment.healthChecks(),
            Collections.emptyList(),
            Tracing.current());
  }
}
