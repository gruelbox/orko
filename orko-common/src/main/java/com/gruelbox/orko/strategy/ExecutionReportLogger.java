package com.gruelbox.orko.strategy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.exchange.ExchangeConfiguration;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.util.SafelyDispose;

import info.bitrich.xchangestream.binance.BinanceStreamingExchange;
import info.bitrich.xchangestream.binance.dto.ExecutionReportBinanceUserTransaction;
import info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange;
import info.bitrich.xchangestream.bitfinex.dto.BitfinexWebSocketAuthTrade;
import io.dropwizard.lifecycle.Managed;
import io.reactivex.disposables.Disposable;

/**
 * Debug support. For working out why some streamed trades are coming out with the wrong sign.
 *
 * @author Graham Crockford
 */
@Singleton
class ExecutionReportLogger implements Managed {

  private final NotificationService notificationService;
  private final ExchangeService exchangeService;
  private final OrkoConfiguration orkoConfiguration;

  private Disposable binance;
  private Disposable bitfinex;

  @Inject
  ExecutionReportLogger(ExchangeService exchangeService, NotificationService notificationService, OrkoConfiguration orkoConfiguration) {
    this.exchangeService = exchangeService;
    this.notificationService = notificationService;
    this.orkoConfiguration = orkoConfiguration;
  }

  @Override
  public void start() throws Exception {
    if (orkoConfiguration.getExchanges() != null) {
      ExchangeConfiguration binanceConfig = orkoConfiguration.getExchanges().get(Exchanges.BINANCE);
      if (binanceConfig != null && binanceConfig.isAuthenticated()) {
        BinanceStreamingExchange binanceExchange = (BinanceStreamingExchange) exchangeService.get(Exchanges.BINANCE);
        binance = binanceExchange.getStreamingTradeService()
            .getRawExecutionReports()
            .subscribe(this::onExecutionReport);
      }
      ExchangeConfiguration bitfinexConfig = orkoConfiguration.getExchanges().get(Exchanges.BITFINEX);
      if (bitfinexConfig != null && bitfinexConfig.isAuthenticated()) {
        BitfinexStreamingExchange bitfinexExchange = (BitfinexStreamingExchange) exchangeService.get(Exchanges.BITFINEX);
        bitfinex = bitfinexExchange.getStreamingTradeService()
            .getRawAuthenticatedTrades()
            .subscribe(this::onAuthenticatedTrade);
      }
    }
  }

  @Override
  public void stop() throws Exception {
    SafelyDispose.of(binance, bitfinex);
  }

  private void onExecutionReport(ExecutionReportBinanceUserTransaction e) {
    notificationService.info(String.format("Binance execution report: %s", e));
  }

  private void onAuthenticatedTrade(BitfinexWebSocketAuthTrade e) {
    notificationService.info(String.format("Bitfinex authenticated trade: %s", e));
  }
}