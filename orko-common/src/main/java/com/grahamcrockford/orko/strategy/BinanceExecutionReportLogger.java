package com.grahamcrockford.orko.strategy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.marketdata.MarketDataSubscriptionManager;
import com.grahamcrockford.orko.notification.NotificationService;
import com.grahamcrockford.orko.util.SafelyDispose;

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