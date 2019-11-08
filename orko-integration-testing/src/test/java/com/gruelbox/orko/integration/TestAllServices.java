package com.gruelbox.orko.integration;

import static java.math.BigDecimal.ZERO;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.knowm.xchange.dto.Order.OrderType.BID;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.Balance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.gruelbox.orko.app.marketdata.MarketDataAppConfiguration;
import com.gruelbox.orko.app.marketdata.MarketDataApplication;
import com.gruelbox.orko.app.monolith.MonolithApplication;
import com.gruelbox.orko.app.monolith.MonolithConfiguration;
import com.gruelbox.orko.exchange.ExchangeResource;
import com.gruelbox.orko.exchange.MarketDataSubscription;
import com.gruelbox.orko.exchange.MarketDataType;
import com.gruelbox.orko.exchange.RemoteMarketDataConfiguration;
import com.gruelbox.orko.exchange.SubscriptionControllerRemoteImpl;
import com.gruelbox.orko.exchange.SubscriptionPublisher;
import com.gruelbox.orko.spi.TickerSpec;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;

/**
 * Chains together the market data and primary service via websockets and confirms we get
 * data through both.
 */
public class TestAllServices {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestAllServices.class);

  @ClassRule
  public static final DropwizardAppRule<MarketDataAppConfiguration> DATA_APP =
      new DropwizardAppRule<MarketDataAppConfiguration>(
          MarketDataApplication.class,
          ResourceHelpers.resourceFilePath("marketdata-test-config.yml"));

  @ClassRule
  public static final DropwizardAppRule<MonolithConfiguration> MAIN_APP =
      new DropwizardAppRule<MonolithConfiguration>(
          MonolithApplication.class,
          ResourceHelpers.resourceFilePath("main-test-config.yml"));

  private Client client;

  private Set<MarketDataSubscription> subscriptions = Set.of(
      MarketDataSubscription.create(TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.TICKER),
      MarketDataSubscription.create(TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.ORDERBOOK),
      MarketDataSubscription.create(TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.ORDER),
      MarketDataSubscription.create(TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.BALANCE),
      MarketDataSubscription.create(TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.OPEN_ORDERS),
      MarketDataSubscription.create(TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.TRADES),
      MarketDataSubscription.create(TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.USER_TRADE));

  private SubscriptionPublisher publisher = new SubscriptionPublisher();
  private SubscriptionControllerRemoteImpl controller = new SubscriptionControllerRemoteImpl(
      publisher,
      new RemoteMarketDataConfiguration("ws://localhost:8080/ws"));

  private CountDownLatch gotTickers = new CountDownLatch(3);
  private CountDownLatch gotAll = new CountDownLatch(subscriptions.size());
  private Set<MarketDataType> got = Sets.newConcurrentHashSet();

  @Before
  public void setup() throws Exception {
    publisher.getTickers().map(t -> MarketDataType.TICKER).subscribe(type -> {
      received(type);
      gotTickers.countDown();
    });
    publisher.getBalances().map(t -> MarketDataType.BALANCE).subscribe(this::received);
    publisher.getOrderBookSnapshots().map(t -> MarketDataType.ORDERBOOK).subscribe(this::received);
    publisher.getOrderChanges().map(t -> MarketDataType.ORDER).subscribe(this::received);
    publisher.getOrderSnapshots().map(t -> MarketDataType.OPEN_ORDERS).subscribe(this::received);
    publisher.getTrades().map(t -> MarketDataType.TRADES).subscribe(this::received);
    publisher.getUserTrades().map(t -> MarketDataType.USER_TRADE).subscribe(this::received);

    client = new JerseyClientBuilder(MAIN_APP.getEnvironment())
        .using(MAIN_APP.getConfiguration().getJerseyClientConfiguration())
        .build("test client");
    controller.start();
  }

  @After
  public void tearDown() throws Exception {
    controller.stop();
  }

  @Test
  public void testWebSockets() {
    try {
      controller.updateSubscriptions(subscriptions);
      waitUntilBalanceAvailable();
      waitUntilHadAFewTickers();
      performTrade();
      confirmAllDataTypesReceived();
    } catch (Exception e) {
      LOGGER.error("Error in main test body", e);
      throw new RuntimeException(e);
    }
  }

  private void received(MarketDataType type) {
    LOGGER.info("Got {}", type);
    if (got.add(type)) {
      gotAll.countDown();
    }
  }

  private void waitUntilHadAFewTickers() throws InterruptedException {
    Assert.assertTrue(gotTickers.await(1, TimeUnit.MINUTES));
  }

  private void waitUntilBalanceAvailable() throws InterruptedException {
    var usdBalance = ZERO;
    var zero = Balance.zero(Currency.getInstance("USD"));
    var stopwatch = Stopwatch.createStarted();
    do {
      Thread.sleep(1000);
      usdBalance = client.target(String.format("http://localhost:%d/main/exchanges/simulated/balance/USD", MAIN_APP.getLocalPort()))
          .request()
          .get(new GenericType<Map<String, Balance>>() { })
          .getOrDefault("USD", zero)
          .getAvailable();
    } while (stopwatch.elapsed(SECONDS) < 30 && usdBalance.compareTo(ZERO) == 0);
    assertNotEquals(0, usdBalance.compareTo(ZERO));
  }

  private void performTrade() {
    var order = new ExchangeResource.OrderPrototype();
    order.setAmount(new BigDecimal("0.01"));
    order.setBase("BTC");
    order.setCounter("USD");
    order.setLimitPrice(new BigDecimal(50_000));
    order.setType(BID);
    var response = client.target(String.format("http://localhost:%d/main/exchanges/simulated/orders", MAIN_APP.getLocalPort()))
        .request()
        .post(Entity.json(order));
    assertEquals("Error: " + response.getEntity().toString(), 200, response.getStatus());
  }

  private void confirmAllDataTypesReceived() throws InterruptedException {
    LOGGER.info("Waiting for receipt");
    Assert.assertTrue(gotAll.await(1, TimeUnit.MINUTES));
  }
}
