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

import com.google.common.collect.ImmutableMap;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TelegramService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TelegramService.class);

  private final WebTarget telegramTarget;
  private final TelegramConfiguration configuration;

  @Inject
  TelegramService(TelegramConfiguration configuration, Client client) {
    this.configuration = configuration;
    this.telegramTarget =
        client.target("https://api.telegram.org/bot" + configuration.getBotToken());
  }

  void sendMessage(String message) {
    try {
      final Response response =
          telegramTarget
              .path("sendMessage")
              .request()
              .post(
                  Entity.entity(
                      ImmutableMap.of("chat_id", configuration.getChatId(), "text", message),
                      MediaType.APPLICATION_JSON));
      if (response.getStatus() != 200) {
        LOGGER.error("Could not send message: {}", response.readEntity(String.class));
      }
    } catch (Exception e) {
      LOGGER.error("Could not send message: {}", e.getMessage(), e);
    }
  }
}
