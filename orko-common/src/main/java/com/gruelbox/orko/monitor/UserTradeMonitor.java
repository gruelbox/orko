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
package com.gruelbox.orko.monitor;

import static com.gruelbox.orko.util.MoreBigDecimals.stripZeros;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.exchange.MarketDataSubscriptionManager;
import com.gruelbox.orko.exchange.UserTradeEvent;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.util.SafelyDispose;
import io.dropwizard.lifecycle.Managed;
import io.reactivex.disposables.Disposable;
import java.util.Date;

@Singleton
class UserTradeMonitor implements Managed {

  private final NotificationService notificationService;
  private final MarketDataSubscriptionManager marketDataSubscriptionManager;

  private Disposable subscription;

  @Inject
  UserTradeMonitor(
      MarketDataSubscriptionManager marketDataSubscriptionManager,
      NotificationService notificationService) {
    this.marketDataSubscriptionManager = marketDataSubscriptionManager;
    this.notificationService = notificationService;
  }

  @Override
  public void start() throws Exception {
    Date startDate = new Date();
    subscription =
        marketDataSubscriptionManager
            .getUserTrades()
            .filter(t -> t.trade().getTimestamp().after(startDate))
            .distinct(t -> t.spec().exchange() + "-" + t.trade().getId())
            .subscribe(this::onUserTrade);
  }

  @Override
  public void stop() throws Exception {
    SafelyDispose.of(subscription);
  }

  @Subscribe
  void onUserTrade(UserTradeEvent e) {
    String message =
        String.format(
            "Trade executed on %s %s/%s market: %s %s at %s",
            e.spec().exchange(),
            e.spec().base(),
            e.spec().counter(),
            e.trade().getType().toString().toLowerCase(),
            stripZeros(e.trade().getOriginalAmount()).toPlainString(),
            stripZeros(e.trade().getPrice()).toPlainString());
    notificationService.alert(message);
  }
}
