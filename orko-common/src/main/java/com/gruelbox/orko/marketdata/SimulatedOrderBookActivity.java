package com.gruelbox.orko.marketdata;

import static java.math.RoundingMode.HALF_UP;
import static org.knowm.xchange.currency.Currency.BTC;
import static org.knowm.xchange.currency.Currency.USD;
import static org.knowm.xchange.currency.CurrencyPair.BTC_USD;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.knowm.xchange.dto.Order.OrderType.BID;
import static org.knowm.xchange.simulated.SimulatedExchange.ACCOUNT_FACTORY_PARAM;
import static org.knowm.xchange.simulated.SimulatedExchange.ENGINE_FACTORY_PARAM;

import java.io.IOException;
import java.math.BigDecimal;

import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.simulated.AccountFactory;
import org.knowm.xchange.simulated.MatchingEngineFactory;
import org.knowm.xchange.simulated.SimulatedExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.exchange.ExchangeConfiguration;
import com.gruelbox.orko.exchange.Exchanges;

/**
 * A background process which places trades back and forth to simulate
 * market activity. Enabled if an API key is provided for
 * {@link SimulatedExchange}.
 *
 * @author Graham Crockford
 */
@Singleton
class SimulatedOrderBookActivity extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimulatedOrderBookActivity.class);

  private final SimulatedExchange marketMakerExchange;
  private final OrkoConfiguration orkoConfiguration;

  @Inject
  SimulatedOrderBookActivity(OrkoConfiguration orkoConfiguration, AccountFactory accountFactory, MatchingEngineFactory matchingEngineFactory) {
    this.orkoConfiguration = orkoConfiguration;
    ExchangeSpecification exchangeSpecification = new ExchangeSpecification(SimulatedExchange.class);
    exchangeSpecification.setApiKey("MarketMakers");
    exchangeSpecification.setExchangeSpecificParametersItem(ENGINE_FACTORY_PARAM, matchingEngineFactory);
    exchangeSpecification.setExchangeSpecificParametersItem(ACCOUNT_FACTORY_PARAM, accountFactory);
    marketMakerExchange = (SimulatedExchange) ExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
  }

  @Override
  protected void run() throws Exception {
    if (!isEnabled())
      return;
    LOGGER.info("Starting market data simulator...");
    RateLimiter rateLimiter = RateLimiter.create(3);
    mockMarket();
    while (isRunning() && !Thread.interrupted()) {
      rateLimiter.acquire();
      LOGGER.debug("Selling...");
      try {
        placeMarketOrder(ASK, new BigDecimal("0.01"));
        placeLimitOrder(BID, marketMakerExchange.getMarketDataService().getTicker(BTC_USD).getBid(), new BigDecimal("0.01"));
      } catch (Exception e) {
        LOGGER.error("Error in simulator", e);
      }
      rateLimiter.acquire();
      LOGGER.debug("Buying...");
      try {
        placeMarketOrder(BID, new BigDecimal("0.01"));
        placeLimitOrder(ASK, marketMakerExchange.getMarketDataService().getTicker(BTC_USD).getAsk(), new BigDecimal("0.01"));
      } catch (Exception e) {
        LOGGER.error("Error in simulator", e);
      }
    }
  }

  private boolean isEnabled() {
    if (orkoConfiguration.getExchanges() == null)
      return false;
    ExchangeConfiguration exchangeConfiguration = orkoConfiguration.getExchanges().get(Exchanges.SIMULATED);
    if (exchangeConfiguration == null || !exchangeConfiguration.isAuthenticated())
      return false;
    return true;
  }

  private void mockMarket() throws IOException {
    marketMakerExchange.getAccountService().deposit(USD, new BigDecimal("99999999999999999"));
    marketMakerExchange.getAccountService().deposit(BTC, new BigDecimal("99999999999999999"));
    BigDecimal startPrice = new BigDecimal(3500);
    BigDecimal range = new BigDecimal(6000);
    BigDecimal seedIncrement = new BigDecimal("0.1");
    BigDecimal multiplicator = new BigDecimal("1.5");
    BigDecimal amountMultiplicator = new BigDecimal(3);

    BigDecimal diff = seedIncrement;
    while (diff.compareTo(range) < 0) {
      LOGGER.debug("Building order book, price diff {}", diff);
      placeLimitOrder(ASK, startPrice.add(diff).setScale(2, HALF_UP), amountMultiplicator.multiply(diff).setScale(2, HALF_UP));
      placeLimitOrder(BID, startPrice.subtract(diff).setScale(2, HALF_UP), amountMultiplicator.multiply(diff).setScale(2, HALF_UP));
      diff = diff.multiply(multiplicator).setScale(2, HALF_UP);
    }
  }

  private void placeLimitOrder(OrderType orderType, BigDecimal price, BigDecimal amount) throws IOException {
    marketMakerExchange.getTradeService().placeLimitOrder(new LimitOrder.Builder(orderType, BTC_USD)
        .limitPrice(price)
        .originalAmount(amount)
        .build());
  }

  private void placeMarketOrder(OrderType orderType, BigDecimal amount) throws IOException {
    marketMakerExchange.getTradeService().placeMarketOrder(new MarketOrder.Builder(orderType, BTC_USD)
        .originalAmount(amount)
        .build());
  }
}
