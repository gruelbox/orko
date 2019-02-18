package com.gruelbox.orko.exchange;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.gruelbox.orko.OrkoConfiguration;

abstract class AbstractExchangeServiceFactory<T> {

  private final OrkoConfiguration configuration;

  AbstractExchangeServiceFactory(OrkoConfiguration configuration) {
    this.configuration = configuration;
  }

  public T getForExchange(String exchange) {
    Map<String, ExchangeConfiguration> exchangeConfig = configuration.getExchanges();
    if (exchangeConfig == null) {
      return getPaperFactory().getForExchange(exchange);
    }
    final ExchangeConfiguration exchangeConfiguration = configuration.getExchanges().get(exchange);
    if (exchangeConfiguration == null || StringUtils.isEmpty(exchangeConfiguration.getApiKey())) {
      return getPaperFactory().getForExchange(exchange);
    }
    return getRealFactory().getForExchange(exchange);
  }

  protected abstract ExchangeServiceFactory<T> getRealFactory();

  protected abstract ExchangeServiceFactory<T> getPaperFactory();
}