package com.grahamcrockford.orko.notification;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

class TelegramService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TelegramService.class);

  private static AtomicBoolean warnedNotConfigured = new AtomicBoolean();

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
    if (telegramTarget == null) {
      if (warnedNotConfigured.compareAndSet(false, true))
        LOGGER.warn("Telegram message suppressed. Not configured.");
      return;
    }
    final Response response = telegramTarget
      .path("sendMessage")
      .request()
      .post(
        Entity.entity(
          ImmutableMap.of(
              "chat_id", configuration.getChatId(),
              "text", message
          ),
          MediaType.APPLICATION_JSON
        )
      );
    if (response.getStatus() != 200) {
      LOGGER.error("Could not send telegram message: " + response.getEntity());
    }
  }
}