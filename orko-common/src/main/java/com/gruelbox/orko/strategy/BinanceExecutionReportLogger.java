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

package com.gruelbox.orko.strategy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.marketdata.MarketDataSubscriptionManager;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.util.SafelyDispose;

import info.bitrich.xchangestream.binance.dto.ExecutionReportBinanceUserTransaction;
import io.dropwizard.lifecycle.Managed;
import io.reactivex.disposables.Disposable;

@Singleton
class BinanceExecutionReportLogger implements Managed {

  private final MarketDataSubscriptionManager marketDataSubscriptionManager;
  private Disposable disposable;
  private final NotificationService notificationService;

  @Inject
  BinanceExecutionReportLogger(MarketDataSubscriptionManager marketDataSubscriptionManager, NotificationService notificationService) {
    this.marketDataSubscriptionManager = marketDataSubscriptionManager;
    this.notificationService = notificationService;
  }

  @Override
  public void start() throws Exception {
    disposable = marketDataSubscriptionManager.getBinanceExecutionReports().subscribe(this::onExecutionReport);
  }

  @Override
  public void stop() throws Exception {
    SafelyDispose.of(disposable);
  }

  private void onExecutionReport(ExecutionReportBinanceUserTransaction e) {
    if (e.getTradeId() == -1) {
      notificationService.info(String.format("Binance execution report: %s order [%d] on %s at %s",
          e.getExecutionType(), e.getOrderId(), e.getCurrencyPair(), e.getOrderPrice()));
    } else {
      notificationService.info(String.format("Binance execution report: %s order [%d] on %s at %s (tradeId=%d, %s of %s at %s)",
          e.getExecutionType(), e.getOrderId(), e.getCurrencyPair(), e.getOrderPrice(),
          e.getTradeId(), e.getLastExecutedQuantity(), e.getOrderQuantity(), e.getLastExecutedPrice()));
    }
  }
}