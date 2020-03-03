/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.exchange;

import static com.gruelbox.orko.exchange.Exchanges.BINANCE;
import static com.gruelbox.orko.exchange.Exchanges.BITFINEX;
import static com.gruelbox.orko.exchange.Exchanges.BITMEX;
import static com.gruelbox.orko.exchange.Exchanges.BITTREX;
import static com.gruelbox.orko.exchange.Exchanges.GDAX;
import static com.gruelbox.orko.exchange.Exchanges.KRAKEN;
import static com.gruelbox.orko.exchange.Exchanges.KUCOIN;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import info.bitrich.xchangestream.coinbasepro.CoinbaseProStreamingExchange;
import java.util.Map;
import org.junit.Test;
import org.knowm.xchange.kucoin.KucoinExchange;
import org.knowm.xchange.simulated.AccountFactory;
import org.knowm.xchange.simulated.MatchingEngineFactory;
import org.mockito.Mockito;

public class TestExchangeService {

  @Test
  public void testGdax() {
    ExchangeServiceImpl exchangeService = vanilla(GDAX);
    assertTrue(exchangeService.getExchanges().contains(GDAX));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInstantiationFailurePublic() {
    ExchangeServiceImpl exchangeService = vanilla("nonexistent");
    exchangeService.get("nonexistent");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInstantiationFailurePrivate() {
    Map<String, ExchangeConfiguration> config = baseConfig("nonexistent");
    config.get("nonexistent").setApiKey("XXX");
    ExchangeServiceImpl exchangeService = of(config);
    exchangeService.get("nonexistent");
  }

  @Test
  public void testGdaxSandbox() {
    Map<String, ExchangeConfiguration> config = baseConfig(GDAX);
    config.get(GDAX).setSandbox(true);
    ExchangeServiceImpl exchangeService = of(config);
    assertTrue(exchangeService.getExchanges().contains(GDAX));
    assertTrue(exchangeService.get(GDAX) instanceof CoinbaseProStreamingExchange);
  }

  @Test
  public void testBinance() {
    ExchangeServiceImpl exchangeService = vanilla(BINANCE);
    assertTrue(exchangeService.getExchanges().contains(BINANCE));
  }

  @Test
  public void testBitfinex() {
    ExchangeServiceImpl exchangeService = vanilla(BITFINEX);
    assertTrue(exchangeService.getExchanges().contains(BITFINEX));
  }

  @Test
  public void testBittrex() {
    ExchangeServiceImpl exchangeService = vanilla(BITTREX);
    assertTrue(exchangeService.getExchanges().contains(BITTREX));
  }

  @Test
  public void testBitmex() {
    ExchangeServiceImpl exchangeService = vanilla(BITMEX);
    assertTrue(exchangeService.getExchanges().contains(BITMEX));
  }

  @Test
  public void testKraken() {
    ExchangeServiceImpl exchangeService = vanilla(KRAKEN);
    assertTrue(exchangeService.getExchanges().contains(KRAKEN));
  }

  @Test
  public void testKucoinVanilla() {
    ExchangeServiceImpl exchangeService = vanilla(KUCOIN);
    assertTrue(exchangeService.getExchanges().contains(KUCOIN));
    assertTrue(exchangeService.get(KUCOIN) instanceof KucoinExchange);
  }

  @Test
  public void testKucoinSandbox() {
    Map<String, ExchangeConfiguration> config = baseConfig(KUCOIN);
    config.get(KUCOIN).setSandbox(true);
    ExchangeServiceImpl exchangeService = of(config);
    assertTrue(exchangeService.getExchanges().contains(KUCOIN));
    assertTrue(exchangeService.get(KUCOIN) instanceof KucoinExchange);
  }

  private Map<String, ExchangeConfiguration> baseConfig(String exchange) {
    ImmutableMap<String, ExchangeConfiguration> exchanges =
        ImmutableMap.of(exchange, new ExchangeConfiguration());
    exchanges.get(exchange).setLoadRemoteData(false);
    return exchanges;
  }

  private ExchangeServiceImpl vanilla(String exchange) {
    return of(baseConfig(exchange));
  }

  private ExchangeServiceImpl of(Map<String, ExchangeConfiguration> config) {
    ExchangeServiceImpl exchangeService =
        new ExchangeServiceImpl(
            config, Mockito.mock(AccountFactory.class), Mockito.mock(MatchingEngineFactory.class));
    return exchangeService;
  }
}
