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
import org.junit.Ignore;
import org.junit.Test;

import com.gruelbox.orko.OrkoConfiguration;

public class TestExchangeService {

  private ExchangeServiceImpl exchangeService;

  @Before
  public void setup() {
    exchangeService = new ExchangeServiceImpl(new OrkoConfiguration());
  }

  @Test
  public void testGdax() {
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.GDAX));
  }

  @Test
  public void testGdaxSandbox() {
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.GDAX_SANDBOX));
  }

  @Test
  public void testBinance() {
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.BINANCE));
  }

  @Test
  public void testBitfinex() {
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.BITFINEX));
  }

  @Test
  public void testBittrex() {
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.BITTREX));
  }

  @Test
  public void testBitmex() {
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.BITMEX));
  }

  @Test
  public void testKraken() {
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.KRAKEN));
  }

  @Test
  @Ignore // TODO add this back when 2.0 is working
  public void testKucoin() {
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.KUCOIN));
  }
}