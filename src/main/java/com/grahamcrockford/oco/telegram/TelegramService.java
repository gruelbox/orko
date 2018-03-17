package com.grahamcrockford.oco.telegram;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelegramService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TelegramService.class);

  private final WebTarget telegramTarget;
  private final TelegramConfiguration configuration;

  @Inject
  TelegramService(@Nullable TelegramConfiguration configuration, Client client) {
    this.configuration = configuration;
    this.telegramTarget = configuration == null || StringUtils.isEmpty(configuration.getBotToken())
        ? null
        : client.target("https://api.telegram.org/bot" + configuration.getBotToken());
  }

  public void sendMessage(String message) {
    if (telegramTarget != null) {
      final Response response = telegramTarget
        .path("sendMessage")
        .queryParam("chat_id", configuration.getChatId())
        .queryParam("text", message)
        .request()
        .get();
      if (response.getStatus() != 200) {
        LOGGER.error("Could not send telegram message: " + response.getEntity());
      }
    }
  }
}