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

package com.gruelbox.orko.notification;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;

class AppriseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppriseService.class);

  private final WebTarget target;
  private final AppriseConfiguration configuration;

  @Inject
  AppriseService(AppriseConfiguration configuration, Environment environment) {
    this.configuration = configuration;

    // Use a custom Jersey client as the microservice is pretty sensitive to encoding
    JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
    jerseyClientConfiguration.setTimeout(Duration.seconds(10));
    jerseyClientConfiguration.setConnectionTimeout(Duration.seconds(10));
    jerseyClientConfiguration.setConnectionRequestTimeout(Duration.seconds(10));
    jerseyClientConfiguration.setGzipEnabled(false);
    jerseyClientConfiguration.setGzipEnabledForRequests(false);
    Client client = new JerseyClientBuilder(environment)
        .using(jerseyClientConfiguration)
        .build("apprise");

    this.target = client.target(configuration.getMicroserviceUrl());
  }

  void send(Notification notification) {
    try {
      final Response response = target
          .path("/")
          .request()
          .header(HttpHeaders.CONTENT_ENCODING, "identity")
          .post(
              Entity.entity(
                  Map.of(
                      "title", "",
                      "body", notification.message()
                  ),
                  MediaType.APPLICATION_JSON
              )
          );
      if (response.getStatus() != 200) {
        LOGGER.error("Could not send message: {}", response.readEntity(String.class));
      }
    } catch (Exception e) {
      LOGGER.error("Could not send message: {}", e.getMessage(), e);
    }
  }
}