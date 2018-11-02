package com.grahamcrockford.orko.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.marketdata.MarketDataSubscriptionManager;
import com.grahamcrockford.orko.util.SafelyDispose;

import info.bitrich.xchangestream.binance.dto.ExecutionReportBinanceUserTransaction;
import io.dropwizard.lifecycle.Managed;
import io.reactivex.disposables.Disposable;

@Singleton
class BinanceExecutionReportLogger implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(BinanceExecutionReportLogger.class);

  private final MarketDataSubscriptionManager marketDataSubscriptionManager;
  private Disposable disposable;

  @Inject
  BinanceExecutionReportLogger(MarketDataSubscriptionManager marketDataSubscriptionManager) {
    this.marketDataSubscriptionManager = marketDataSubscriptionManager;
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
      LOGGER.info("Binance execution report: {} order [{}] on {} at {}",
          e.getExecutionType(), e.getOrderId(), e.getCurrencyPair(), e.getOrderPrice());
    } else {
      LOGGER.info("Binance execution report: {} order [{}] on {} at {} (tradeId={}, {} of {} at {})",
          e.getExecutionType(), e.getOrderId(), e.getCurrencyPair(), e.getOrderPrice(),
          e.getTradeId(), e.getLastExecutedQuantity(), e.getOrderQuantity(), e.getLastExecutedPrice());
    }
  }
}