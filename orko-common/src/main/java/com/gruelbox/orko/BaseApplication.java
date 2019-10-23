/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gruelbox.orko;

import java.util.Collections;

import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.gruelbox.orko.docker.DockerSecretSubstitutor;
import com.gruelbox.tools.dropwizard.guice.GuiceBundle;
import com.gruelbox.tools.dropwizard.guice.hibernate.GuiceHibernateModule;
import com.gruelbox.tools.dropwizard.guice.hibernate.HibernateBundleFactory;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.kafka.KafkaConsumerBundle;
import io.dropwizard.kafka.KafkaConsumerFactory;
import io.dropwizard.kafka.KafkaProducerBundle;
import io.dropwizard.kafka.KafkaProducerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public abstract class BaseApplication extends Application<OrkoConfiguration> {

  private DockerSecretSubstitutor dockerSecretSubstitutor;

  @Override
  public void initialize(final Bootstrap<OrkoConfiguration> bootstrap) {

    dockerSecretSubstitutor = new DockerSecretSubstitutor(false, false, true);
    bootstrap.setConfigurationSourceProvider(
      new SubstitutingSourceProvider(
        new SubstitutingSourceProvider(
          bootstrap.getConfigurationSourceProvider(),
          new EnvironmentVariableSubstitutor(false)
        ),
        dockerSecretSubstitutor
      )
    );

    HibernateBundleFactory<OrkoConfiguration> hibernateBundleFactory
      = new HibernateBundleFactory<>(configuration -> configuration.getDatabase().toDataSourceFactory());

    KafkaProducerBundle<String, String, OrkoConfiguration> kafkaProducer = new KafkaProducerBundle<>(Collections.emptyList()) {
      @Override
      public KafkaProducerFactory<String, String> getKafkaProducerFactory(OrkoConfiguration configuration) {
        return configuration.getKafkaProducerFactory();
      }

      public void run(OrkoConfiguration configuration, Environment environment) throws Exception {
        if (!configuration.isRemoteData()) {
          return;
        }
        super.run(configuration, environment);
      };
    };

    KafkaConsumerBundle<String, String, OrkoConfiguration> kafkaConsumer = new KafkaConsumerBundle<>(Collections.emptyList(), new NoOpConsumerRebalanceListener()) {
      @Override
      public KafkaConsumerFactory<String, String> getKafkaConsumerFactory(OrkoConfiguration configuration) {
          return configuration.getKafkaConsumerFactory();
      }

      public void run(OrkoConfiguration configuration, Environment environment) throws Exception {
        if (!configuration.isRemoteData()) {
          return;
        }
        super.run(configuration, environment);
      };
    };

    bootstrap.addBundle(
      new GuiceBundle<OrkoConfiguration>(
        this,
        new OrkoApplicationModule(),
        new GuiceHibernateModule(hibernateBundleFactory),
        binder -> {
          binder.bind(new TypeLiteral<KafkaProducerBundle<String, String, OrkoConfiguration>>() {}).toInstance(kafkaProducer);
          binder.bind(new TypeLiteral<KafkaConsumerBundle<String, String, OrkoConfiguration>>() {}).toInstance(kafkaConsumer);
        },
        createApplicationModule()
      )
    );

    bootstrap.addBundle(hibernateBundleFactory.bundle());
    bootstrap.addBundle(kafkaProducer);
    bootstrap.addBundle(kafkaConsumer);
  }

  protected abstract Module createApplicationModule();

  @Override
  public void run(OrkoConfiguration configuration, Environment environment) throws Exception {
    Logger dockerLogger = LoggerFactory.getLogger(DockerSecretSubstitutor.class);
    if (dockerLogger.isDebugEnabled()) {
      dockerSecretSubstitutor.getLog().stream()
          .forEach(entry -> dockerLogger.debug(entry));
    }
  }
}