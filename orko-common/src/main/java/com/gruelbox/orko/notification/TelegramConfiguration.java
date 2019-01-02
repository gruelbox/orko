package com.gruelbox.orko.notification;

/*-
 * ===============================================================================L
 * Orko Common
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

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
