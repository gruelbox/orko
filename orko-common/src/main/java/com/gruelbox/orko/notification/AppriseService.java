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

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

class AppriseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppriseService.class);

  private final WebTarget target;
  private final AppriseConfiguration configuration;

  @Inject
  AppriseService(AppriseConfiguration configuration, Client client) {
    this.configuration = configuration;
    this.target = client.target(configuration.getMicroServiceUrl());
  }

  void send(Notification notification) {
    final Response response = target
      .path("")
      .request()
      .post(
        Entity.entity(
          ImmutableMap.of(
              "title", notification.level().name(),
              "body", notification.message()
          ),
          MediaType.APPLICATION_JSON
        )
      );
    if (response.getStatus() != 200) {
      LOGGER.error("Could not send message: {}", response.getEntity());
    }
  }
}