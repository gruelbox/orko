package com.grahamcrockford.oco.core;

import org.knowm.xchange.service.trade.TradeService;
import com.google.inject.Inject;
import com.grahamcrockford.oco.OcoConfiguration;

/**
 * Actually allows live trading.
 */
public class LiveTradeServiceFactory implements TradeServiceFactory {

  private final ExchangeService exchangeService;
  private final OcoConfiguration configuration;
  private final PaperTradeService.Factory paperTradeServiceFactory;

  @Inject
  LiveTradeServiceFactory(ExchangeService exchangeService, OcoConfiguration configuration, PaperTradeService.Factory paperTradeServiceFactory) {
    this.exchangeService = exchangeService;
    this.configuration = configuration;
    this.paperTradeServiceFactory = paperTradeServiceFactory;
  }

  @Override
  public TradeService getForExchange(String exchange) {
    final ExchangeConfiguration exchangeConfiguration = configuration.getExchanges().get(exchange);
    if (exchangeConfiguration == null) {
      return paperTradeServiceFactory.getForExchange(exchange);
    }
    return exchangeService.get(exchange).getTradeService();
  }
}