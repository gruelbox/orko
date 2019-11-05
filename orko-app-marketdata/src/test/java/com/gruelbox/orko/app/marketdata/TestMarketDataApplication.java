package com.gruelbox.orko.app.marketdata;

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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.Balance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.gruelbox.orko.exchange.ExchangeResource;
import com.gruelbox.orko.exchange.MarketDataSubscription;
import com.gruelbox.orko.exchange.MarketDataType;
import com.gruelbox.orko.exchange.RemoteMarketDataConfiguration;
import com.gruelbox.orko.exchange.SubscriptionControllerRemoteImpl;
import com.gruelbox.orko.exchange.SubscriptionPublisher;
import com.gruelbox.orko.spi.TickerSpec;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;

public class TestMarketDataApplication {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestMarketDataApplication.class);

  private static final DropwizardTestSupport<MarketDataAppConfiguration> SUPPORT =
      new DropwizardTestSupport<MarketDataAppConfiguration>(MarketDataApplication.class,
          ResourceHelpers.resourceFilePath("test-config.yml"));

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

  private CountDownLatch gotTicker = new CountDownLatch(1);
  private CountDownLatch gotAll = new CountDownLatch(subscriptions.size());
  private Set<MarketDataType> got = Sets.newConcurrentHashSet();

  @Before
  public void setup() {
    publisher.getTickers().map(t -> MarketDataType.TICKER).subscribe(type -> {
      received(type);
      gotTicker.countDown();
    });
    publisher.getBalances().map(t -> MarketDataType.BALANCE).subscribe(this::received);
    publisher.getOrderBookSnapshots().map(t -> MarketDataType.ORDERBOOK).subscribe(this::received);
    publisher.getOrderChanges().map(t -> MarketDataType.ORDER).subscribe(this::received);
    publisher.getOrderSnapshots().map(t -> MarketDataType.OPEN_ORDERS).subscribe(this::received);
    publisher.getTrades().map(t -> MarketDataType.TRADES).subscribe(this::received);
    publisher.getUserTrades().map(t -> MarketDataType.USER_TRADE).subscribe(this::received);
  }

  @Test
  public void testWithServerReady() throws Exception {
    SUPPORT.before();
    try {
      client = new JerseyClientBuilder(SUPPORT.getEnvironment()).build("test client");
      controller.start();
      try {
        controller.updateSubscriptions(subscriptions);
        waitUntilBalanceAvailable();
        waitUntilHadATicker();
        performTrade();
        confirmAllDataTypesReceived();
      } finally {
        controller.stop();
      }
    } finally {
      SUPPORT.after();
    }
  }
  @Test
  public void testWithServerStartDelayed() throws Exception {
    controller.start();
    try {
      controller.updateSubscriptions(subscriptions);
      Thread.sleep(3000);
      LOGGER.info("Starting server");
      SUPPORT.before();
      try {
        client = new JerseyClientBuilder(SUPPORT.getEnvironment()).build("test client");
        waitUntilBalanceAvailable();
        waitUntilHadATicker();
        performTrade();
        confirmAllDataTypesReceived();
      } finally {
        SUPPORT.after();
      }
    } finally {
      controller.stop();
    }
  }


  private void received(MarketDataType type) {
    if (got.add(type)) {
      LOGGER.info("Got {}", type);
      gotAll.countDown();
    }
  }

  private void waitUntilHadATicker() throws InterruptedException {
    Assert.assertTrue(gotTicker.await(1, TimeUnit.MINUTES));
  }

  private void waitUntilBalanceAvailable() throws InterruptedException {
    var usdBalance = ZERO;
    var zero = Balance.zero(Currency.getInstance("USD"));
    var stopwatch = Stopwatch.createStarted();
    do {
      Thread.sleep(1000);
      usdBalance = client.target(String.format("http://localhost:%d/exchanges/simulated/balance/USD", SUPPORT.getLocalPort()))
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
    var response = client.target(String.format("http://localhost:%d/exchanges/simulated/orders", SUPPORT.getLocalPort()))
        .request()
        .post(Entity.json(order));
    assertEquals("Error: " + response.getEntity().toString(), 200, response.getStatus());
  }

  private void confirmAllDataTypesReceived() throws InterruptedException {
    LOGGER.info("Waiting for receipt");
    Assert.assertTrue(gotAll.await(1, TimeUnit.MINUTES));
  }
}