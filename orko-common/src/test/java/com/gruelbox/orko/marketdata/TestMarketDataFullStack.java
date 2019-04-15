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

import static com.gruelbox.orko.marketdata.MarketDataType.BALANCE;
import static com.gruelbox.orko.marketdata.MarketDataType.OPEN_ORDERS;
import static com.gruelbox.orko.marketdata.MarketDataType.ORDERBOOK;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static com.gruelbox.orko.marketdata.MarketDataType.TRADES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.knowm.xchange.simulated.AccountFactory;
import org.knowm.xchange.simulated.MatchingEngineFactory;

import com.google.common.collect.ImmutableSet;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.exchange.ExchangeConfiguration;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.exchange.ExchangeServiceImpl;
import com.gruelbox.orko.exchange.Exchanges;
import com.gruelbox.orko.spi.TickerSpec;

/**
 * Stack tests for {@link MarketDataSubscriptionManager} which use the simulated
 * exchange. These can run as part of the main build.
 */
public class TestMarketDataFullStack extends AbstractMarketDataFullStackTest {

  private static final TickerSpec SIMULATED = TickerSpec.builder().base("BTC").counter("USD").exchange(Exchanges.SIMULATED).build();

  private SimulatedOrderBookActivity simulator;
  private AccountFactory accountFactory;
  private MatchingEngineFactory matchingEngineFactory;

  @Override
  public void setup() throws TimeoutException {
    accountFactory = new AccountFactory();
    matchingEngineFactory = new MatchingEngineFactory(accountFactory);
    simulator = new SimulatedOrderBookActivity(accountFactory, matchingEngineFactory);
    simulator.startAsync().awaitRunning(30, SECONDS);
    super.setup();
  }

  @Override
  public void tearDown() throws TimeoutException {
    super.tearDown();
    simulator.stopAsync().awaitTerminated(30, SECONDS);
  }

  @Override
  protected OrkoConfiguration buildConfig() {
    OrkoConfiguration config = super.buildConfig();
    config.setExchanges(new HashMap<>());
    Exchanges.EXCHANGE_TYPES.get().forEach(clazz -> {
      String name = Exchanges.classToFriendlyName(clazz);
      ExchangeConfiguration exchangeConfiguration = new ExchangeConfiguration();
      exchangeConfiguration.setLoadRemoteData(false);
      if (name.equals(Exchanges.SIMULATED)) {
        exchangeConfiguration.setApiKey("Test");
      }
      config.getExchanges().put(name, exchangeConfiguration);
    });
    return config;
  }

  @Override
  protected ExchangeService buildExchangeService() {
    return new ExchangeServiceImpl(orkoConfiguration,
        accountFactory,
        matchingEngineFactory);
  }

  @Override
  protected Set<MarketDataSubscription> subscriptions() {
    return ImmutableSet.of(
        MarketDataSubscription.create(SIMULATED, TICKER),
        MarketDataSubscription.create(SIMULATED, ORDERBOOK),
        MarketDataSubscription.create(SIMULATED, TRADES),
        MarketDataSubscription.create(SIMULATED, BALANCE),
        MarketDataSubscription.create(SIMULATED, OPEN_ORDERS)
      );
  }

  @Override
  protected MarketDataSubscription ticker() {
    return MarketDataSubscription.create(SIMULATED, TICKER);
  }
}
