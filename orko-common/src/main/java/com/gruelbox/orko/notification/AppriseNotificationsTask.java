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

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.dropwizard.lifecycle.Managed;

@Singleton
class AppriseNotificationsTask implements Managed {

  @Nullable
  private final AppriseConfiguration configuration;
  private final Provider<AppriseService> appriseService;
  private final EventBus eventBus;

  @Inject
  AppriseNotificationsTask(@Nullable AppriseConfiguration configuration, Provider<AppriseService> appriseService, EventBus eventBus) {
    this.configuration = configuration;
    this.appriseService = appriseService;
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
    appriseService.get().send(notification);
  }

  private boolean isEnabled() {
    return configuration != null && StringUtils.isNotBlank(configuration.getMicroServiceUrl());
  }
}