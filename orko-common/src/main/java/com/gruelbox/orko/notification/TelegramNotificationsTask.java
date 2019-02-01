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

import static com.gruelbox.orko.notification.NotificationLevel.ALERT;
import static com.gruelbox.orko.notification.NotificationLevel.ERROR;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.dropwizard.lifecycle.Managed;

@Singleton
final class TelegramNotificationsTask implements Managed {

  private static final Set<NotificationLevel> SEND_FOR = ImmutableSet.of(ERROR, ALERT);

  private final TelegramService telegramService;
  private final EventBus eventBus;

  @Inject
  TelegramNotificationsTask(TelegramService telegramService, EventBus eventBus) {
    this.telegramService = telegramService;
    this.eventBus = eventBus;
  }

  @Override
  public void start() throws Exception {
    eventBus.register(this);
  }

  @Override
  public void stop() throws Exception {
    eventBus.unregister(this);
  }

  @Subscribe
  void notify(Notification notification) {
    if (SEND_FOR.contains(notification.level())) {
      telegramService.sendMessage(notification.message());
    }
  }
}