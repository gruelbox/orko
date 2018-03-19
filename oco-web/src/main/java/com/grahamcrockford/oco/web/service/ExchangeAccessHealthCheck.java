package com.grahamcrockford.oco.web.service;

import java.io.IOException;

import org.knowm.xchange.dto.marketdata.Ticker;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Just attempts to access binance.
 *
 * @author grahamc (Graham Crockford)
 */
@Singleton
class ExchangeAccessHealthCheck extends HealthCheck {

  private final ExchangeResource exchangeResource;

  @Inject
  ExchangeAccessHealthCheck(ExchangeResource exchangeResource) {
    this.exchangeResource = exchangeResource;
  }

  @Override
  protected Result check() throws Exception {
    try {
      Ticker ticker = exchangeResource.ticker("binance", "USDT", "BTC");
      if (ticker.getLast() == null)
        return Result.unhealthy("No price returned");
    } catch (IOException e) {
      return Result.unhealthy(e);
    }
    return Result.healthy();
  }
}