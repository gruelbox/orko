package com.grahamcrockford.oco.exchange;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.service.trade.TradeService;

import com.google.inject.Inject;
import com.grahamcrockford.oco.OcoConfiguration;

class TradeServiceFactoryImpl implements TradeServiceFactory {

  private final ExchangeService exchangeService;
  private final OcoConfiguration configuration;
  private final PaperTradeService.Factory paperTradeServiceFactory;

  @Inject
  TradeServiceFactoryImpl(ExchangeService exchangeService, OcoConfiguration configuration, PaperTradeService.Factory paperTradeServiceFactory) {
    this.exchangeService = exchangeService;
    this.configuration = configuration;
    this.paperTradeServiceFactory = paperTradeServiceFactory;
  }

  @Override
  public TradeService getForExchange(String exchange) {
    Map<String, ExchangeConfiguration> exchangeConfig = configuration.getExchanges();
    if (exchangeConfig == null) {
      return paperTradeServiceFactory.getForExchange(exchange);
    }
    final ExchangeConfiguration exchangeConfiguration = configuration.getExchanges().get(exchange);
    if (exchangeConfiguration == null || StringUtils.isEmpty(exchangeConfiguration.getApiKey())) {
      return paperTradeServiceFactory.getForExchange(exchange);
    }
    return exchangeService.get(exchange).getTradeService();
  }
}