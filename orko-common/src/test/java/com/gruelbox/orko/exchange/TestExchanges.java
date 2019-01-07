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
import org.junit.Test;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.cryptopia.CryptopiaExchange;
import org.knowm.xchange.kucoin.KucoinExchange;

import info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange;
import info.bitrich.xchangestream.coinbasepro.CoinbaseProStreamingExchange;

public class TestExchanges {

  @Test
  public void testGdaxSandbox() {
    Assert.assertEquals(CoinbaseProStreamingExchange.class, Exchanges.friendlyNameToClass(Exchanges.GDAX_SANDBOX));
  }

  @Test
  public void testGdax() {
    Assert.assertEquals(CoinbaseProStreamingExchange.class, Exchanges.friendlyNameToClass(Exchanges.GDAX));
  }

  @Test
  public void testBitfinex() {
    Assert.assertEquals(BitfinexStreamingExchange.class, Exchanges.friendlyNameToClass(Exchanges.BITFINEX));
  }

  @Test
  public void testKucoin() {
    Assert.assertEquals(KucoinExchange.class, Exchanges.friendlyNameToClass(Exchanges.KUCOIN));
  }

  @Test
  public void testBittrex() {
    Assert.assertEquals(BittrexExchange.class, Exchanges.friendlyNameToClass(Exchanges.BITTREX));
  }

  @Test
  public void testCryptopia() {
    Assert.assertEquals(CryptopiaExchange.class, Exchanges.friendlyNameToClass(Exchanges.CRYPTOPIA));
  }
}