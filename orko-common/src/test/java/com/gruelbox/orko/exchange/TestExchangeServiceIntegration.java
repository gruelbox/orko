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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.gruelbox.orko.OrkoConfiguration;

@RunWith(Parameterized.class)
public class TestExchangeServiceIntegration {

  @Parameters(name="{0}")
  public static Iterable<? extends Object> data() {
      return Arrays.asList(Exchanges.BINANCE,
          Exchanges.GDAX,
          Exchanges.KUCOIN,
          Exchanges.BITTREX,
          Exchanges.BITFINEX,
          Exchanges.BITMEX,
          Exchanges.KRAKEN);
  }

  private final ExchangeService exchangeService = new ExchangeServiceImpl(new OrkoConfiguration());
  private final String exchangeName;

  public TestExchangeServiceIntegration(String exchangeName) {
    this.exchangeName = exchangeName;
  }

  @Test
  public void testExists() {
    assertTrue(exchangeService.getExchanges().contains(exchangeName));
  }

  @Test
  public void testGet() {
    assertNotNull(exchangeService.get(exchangeName));
  }

  @Test
  public void testRateLimit() {
    assertNotNull(exchangeService.rateController(exchangeName));
  }
}