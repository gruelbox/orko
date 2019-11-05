package com.gruelbox.orko.app.marketdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.knowm.xchange.dto.Order.OrderType.BID;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

import org.junit.Assert;
import org.junit.ClassRule;
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
import com.gruelbox.orko.exchange.WSRemoteMarketDataSubscriptionManager;
import com.gruelbox.orko.spi.TickerSpec;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;

public class TestMarketDataApplication {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestMarketDataApplication.class);

  @ClassRule
  public static final DropwizardAppRule<MarketDataAppConfiguration> RULE =
      new DropwizardAppRule<MarketDataAppConfiguration>(MarketDataApplication.class, ResourceHelpers.resourceFilePath("test-config.yml"));

  @Test
  public void testMarketDataApplication() throws TimeoutException, InterruptedException {
    WSRemoteMarketDataSubscriptionManager client = new WSRemoteMarketDataSubscriptionManager(RULE.getConfiguration());
    client.startAsync().awaitRunning(Duration.ofSeconds(30));
    try {

      // Subscribe to everything about BTC/USD
      Set<MarketDataSubscription> subscriptions = Set.of(
          MarketDataSubscription.create(TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.TICKER),
          MarketDataSubscription.create(TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.ORDERBOOK),
          MarketDataSubscription.create(TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.ORDER),
          MarketDataSubscription.create(TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.BALANCE),
          MarketDataSubscription.create(TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.OPEN_ORDERS),
          MarketDataSubscription.create(TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.TRADES),
          MarketDataSubscription.create(TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.USER_TRADE));
      client.updateSubscriptions(subscriptions);

      // Release a latch when we get at least one response for each
      CountDownLatch latch = new CountDownLatch(subscriptions.size());
      Set<MarketDataType> got = Sets.newConcurrentHashSet();
      Consumer<MarketDataType> get = type -> {
          if (got.add(type)) {
          LOGGER.info("Got {}", type);
          latch.countDown();
        }
      };
      client.getTickers().map(t -> MarketDataType.TICKER).subscribe(get::accept);
      client.getBalances().map(t -> MarketDataType.BALANCE).subscribe(get::accept);
      client.getOrderBookSnapshots().map(t -> MarketDataType.ORDERBOOK).subscribe(get::accept);
      client.getOrderChanges().map(t -> MarketDataType.ORDER).subscribe(get::accept);
      client.getOrderSnapshots().map(t -> MarketDataType.OPEN_ORDERS).subscribe(get::accept);
      client.getTrades().map(t -> MarketDataType.TRADES).subscribe(get::accept);
      client.getUserTrades().map(t -> MarketDataType.USER_TRADE).subscribe(get::accept);

      // Wait until there's balance available (the simulator does it)
      BigDecimal usdBalance = BigDecimal.ZERO;
      Balance zero = Balance.zero(Currency.getInstance("USD"));
      Stopwatch stopwatch = Stopwatch.createStarted();
      do {
        Thread.sleep(1000);
        usdBalance = RULE.client().target(String.format("http://localhost:%d/api/exchanges/simulated/balance/USD", RULE.getLocalPort()))
            .request()
            .get(new GenericType<Map<String, Balance>>() { })
            .getOrDefault("USD", zero)
            .getAvailable();
      } while (stopwatch.elapsed(TimeUnit.SECONDS) < 30 && usdBalance.compareTo(BigDecimal.ZERO) == 0);
      assertNotEquals(0, usdBalance.compareTo(BigDecimal.ZERO));

      // Push a purchase through which should update all the private APIs.
      ExchangeResource.OrderPrototype order = new ExchangeResource.OrderPrototype();
      order.setAmount(new BigDecimal("0.01"));
      order.setBase("BTC");
      order.setCounter("USD");
      order.setLimitPrice(new BigDecimal(50_000));
      order.setType(BID);
      var response = RULE.client().target(String.format("http://localhost:%d/api/exchanges/simulated/orders", RULE.getLocalPort()))
          .request()
          .post(Entity.json(order));

      // The purchase should have been successful
      assertEquals("Error: " + response.getEntity().toString(), 200, response.getStatus());

      // We should get all the responses back now
      Assert.assertTrue(latch.await(1, TimeUnit.MINUTES));

    } finally {
      client.stopAsync().awaitTerminated(Duration.ofSeconds(30));
    }
  }
}