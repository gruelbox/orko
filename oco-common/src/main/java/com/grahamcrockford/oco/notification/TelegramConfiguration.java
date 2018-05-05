package com.grahamcrockford.oco.notification;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TelegramConfiguration {

  private String botToken;

  private String chatId;

  @JsonProperty
  public String getBotToken() {
    return botToken;
  }

  @JsonProperty
  public void setBotToken(String botToken) {
    this.botToken = botToken;
  }

  @JsonProperty
  public String getChatId() {
    return chatId;
  }

  @JsonProperty
  public void setChatId(String chatId) {
    this.chatId = chatId;
  }
}