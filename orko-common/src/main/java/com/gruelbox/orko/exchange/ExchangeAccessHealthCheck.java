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

package com.gruelbox.orko.exchange;

import org.knowm.xchange.dto.marketdata.Ticker;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.exchange.ExchangeResource.Pair;

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

    exchangeResource.list().stream().filter(ex -> !Exchanges.GDAX_SANDBOX.equals(ex)).forEach(exchange -> {
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
