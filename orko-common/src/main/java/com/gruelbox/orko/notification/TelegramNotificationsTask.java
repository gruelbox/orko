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

import static com.gruelbox.orko.notification.NotificationLevel.ALERT;
import static com.gruelbox.orko.notification.NotificationLevel.ERROR;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

@Singleton
final class TelegramNotificationsTask implements Managed {

  private static final Set<NotificationLevel> SEND_FOR = ImmutableSet.of(ERROR, ALERT);

  private final TelegramConfiguration configuration;
  private final Provider<TelegramService> telegramService;
  private final EventBus eventBus;

  @Inject
  TelegramNotificationsTask(
      @Nullable TelegramConfiguration configuration,
      Provider<TelegramService> telegramService,
      EventBus eventBus) {
    this.configuration = configuration;
    this.telegramService = telegramService;
    this.eventBus = eventBus;
  }

  @Override
  public void start() throws Exception {
    if (isEnabled()) {
      eventBus.register(this);
    }
  }

  @Override
  public void stop() throws Exception {
    if (isEnabled()) {
      eventBus.unregister(this);
    }
  }

  @Subscribe
  void notify(Notification notification) {
    if (SEND_FOR.contains(notification.level())) {
      telegramService.get().sendMessage(notification.message());
    }
  }

  private boolean isEnabled() {
    return configuration != null && StringUtils.isNotBlank(configuration.getBotToken());
  }
}
