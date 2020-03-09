/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.notification;

import com.google.common.base.Suppliers;
import com.google.inject.Singleton;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import java.util.Map;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class AppriseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppriseService.class);

  private final Supplier<WebTarget> target;

  @Inject
  AppriseService(AppriseConfiguration configuration, Environment environment) {
    this.target =
        Suppliers.memoize(
            () -> {
              // Use a custom Jersey client as the microservice is pretty sensitive to encoding
              JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
              jerseyClientConfiguration.setTimeout(Duration.seconds(10));
              jerseyClientConfiguration.setConnectionTimeout(Duration.seconds(10));
              jerseyClientConfiguration.setConnectionRequestTimeout(Duration.seconds(10));
              jerseyClientConfiguration.setGzipEnabled(false);
              jerseyClientConfiguration.setGzipEnabledForRequests(false);
              Client client =
                  new JerseyClientBuilder(environment)
                      .using(jerseyClientConfiguration)
                      .build("apprise-" + this.hashCode());
              return client.target(configuration.getMicroserviceUrl());
            });
  }

  void send(Notification notification) {
    try {
      final Response response =
          target
              .get()
              .path("/")
              .request()
              .header(HttpHeaders.CONTENT_ENCODING, "identity")
              .post(
                  Entity.entity(
                      Map.of("title", "", "body", notification.message()),
                      MediaType.APPLICATION_JSON));
      if (response.getStatus() != 200) {
        LOGGER.error("Could not send message: {}", response.readEntity(String.class));
      }
    } catch (Exception e) {
      LOGGER.error("Could not send message: {}", e.getMessage(), e);
    }
  }
}
