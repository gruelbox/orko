package com.grahamcrockford.orko.exchange;

import org.knowm.xchange.dto.marketdata.Ticker;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.exchange.ExchangeResource.Pair;

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
    ResultBuilder result = Result.builder().healthy();

    exchangeResource.list().stream().filter(ex -> !"gdax-sandbox".equals(ex)).forEach(exchange -> {
      try {
        Pair pair = Iterables.getFirst(exchangeResource.pairs(exchange), null);
        if (pair == null) {
          result.withDetail(exchange, "No pairs");
          result.unhealthy();
        } else {
          Ticker ticker = exchangeResource.ticker(exchange, pair.counter, pair.base);
          if (ticker.getLast() == null) {
            result.withDetail(exchange + "/" + pair.counter + "/" + pair.base, "Nothing returned");
            result.unhealthy();
          } else {
            result.withDetail(exchange + "/" + pair.counter + "/" + pair.base, "Last price: " + ticker.getLast());
          }
        }
      } catch (Exception e) {
        result.withDetail(exchange, "Exception: " + e.getMessage());
        result.unhealthy(e);
      }
    });

    return result.build();
  }
}