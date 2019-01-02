package com.gruelbox.orko.strategy;

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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.signal.UserTradeEvent;

import io.dropwizard.lifecycle.Managed;

@Singleton
class UserTradeNotifier implements Managed {

  private final EventBus eventBus;
  private final NotificationService notificationService;

  @Inject
  UserTradeNotifier(EventBus eventBus, NotificationService notificationService) {
    this.eventBus = eventBus;
    this.notificationService = notificationService;
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
  void onUserTrade(UserTradeEvent e) {
    String message = String.format(
      "Trade executed on %s %s/%s market: %s %s at %s",
      e.spec().exchange(),
      e.spec().base(),
      e.spec().counter(),
      e.trade().getType().toString().toLowerCase(),
      e.trade().getOriginalAmount(),
      e.trade().getPrice()
    );
    notificationService.alert(message);
  }
}
