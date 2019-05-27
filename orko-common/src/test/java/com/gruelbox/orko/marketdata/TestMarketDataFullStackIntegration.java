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

package com.gruelbox.orko.marketdata;

import static com.gruelbox.orko.marketdata.MarketDataType.ORDERBOOK;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static com.gruelbox.orko.marketdata.MarketDataType.TRADES;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.knowm.xchange.simulated.AccountFactory;
import org.knowm.xchange.simulated.MatchingEngineFactory;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.exchange.ExchangeServiceImpl;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.spi.TickerSpec;

/**
 * Stack tests for {@link MarketDataSubscriptionManager}. Actually connects to exchanges.
 */
public class TestMarketDataFullStackIntegration extends AbstractMarketDataFullStackTest {

  private static final TickerSpec BINANCE = TickerSpec.builder().base("BTC").counter("USDT").exchange(Exchanges.BINANCE).build();
  private static final TickerSpec BITFINEX = TickerSpec.builder().base("BTC").counter("USD").exchange(Exchanges.BITFINEX).build();
  private static final TickerSpec GDAX = TickerSpec.builder().base("BTC").counter("USD").exchange(Exchanges.GDAX).build();
  private static final TickerSpec BITTREX = TickerSpec.builder().base("BTC").counter("USDT").exchange(Exchanges.BITTREX).build();
  private static final TickerSpec KUCOIN = TickerSpec.builder().base("BTC").counter("USDT").exchange(Exchanges.KUCOIN).build();
  private static final TickerSpec KRAKEN = TickerSpec.builder().base("BTC").counter("USD").exchange(Exchanges.KRAKEN).build();
  private static final TickerSpec SIMULATED = TickerSpec.builder().base("BTC").counter("USD").exchange(Exchanges.SIMULATED).build();

  @Override
  protected ExchangeService buildExchangeService() {
    return new ExchangeServiceImpl(orkoConfiguration,
        mock(AccountFactory.class),
        mock(MatchingEngineFactory.class));
  }

  @Override
  protected Set<MarketDataSubscription> subscriptions() {
    return FluentIterable.concat(
      FluentIterable.of(BINANCE, BITFINEX, GDAX, BITTREX, KRAKEN, KUCOIN, SIMULATED)
        .transformAndConcat(spec -> ImmutableSet.of(
          MarketDataSubscription.create(spec, TICKER),
          MarketDataSubscription.create(spec, ORDERBOOK),
          MarketDataSubscription.create(spec, TRADES)
        )),
      ImmutableSet.of(
        MarketDataSubscription.create(TickerSpec.builder().base("ETH").counter("USDT").exchange(Exchanges.BINANCE).build(), TRADES)
      )
    )
    .toSet();
  }

  @Override
  protected MarketDataSubscription ticker() {
    return MarketDataSubscription.create(BINANCE, TICKER);
  }
}
