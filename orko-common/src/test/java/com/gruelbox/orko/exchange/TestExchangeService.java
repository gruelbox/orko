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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gruelbox.orko.OrkoConfiguration;

public class TestExchangeService {

  private ExchangeServiceImpl exchangeService;

  @Before
  public void setup() {
    exchangeService = new ExchangeServiceImpl(new OrkoConfiguration());
  }

  @Test
  public void testExchanges() {
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.GDAX));
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.GDAX_SANDBOX));
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.BINANCE));
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.BITFINEX));
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.KUCOIN));
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.BITTREX));
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.CRYPTOPIA));
  }
}