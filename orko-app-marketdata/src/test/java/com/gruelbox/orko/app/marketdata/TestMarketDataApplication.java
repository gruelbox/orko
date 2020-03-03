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
package com.gruelbox.orko.app.marketdata;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.knowm.xchange.dto.Order.OrderType.BID;

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
import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMarketDataApplication {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestMarketDataApplication.class);

  private static final DropwizardTestSupport<MarketDataAppConfiguration> SUPPORT =
      new DropwizardTestSupport<MarketDataAppConfiguration>(
          MarketDataApplication.class, ResourceHelpers.resourceFilePath("test-config.yml"));

  private Client client;

  private Set<MarketDataSubscription> subscriptions =
      Set.of(
          MarketDataSubscription.create(
              TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.TICKER),
          MarketDataSubscription.create(
              TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.ORDERBOOK),
          MarketDataSubscription.create(
              TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.ORDER),
          MarketDataSubscription.create(
              TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.BALANCE),
          MarketDataSubscription.create(
              TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.OPEN_ORDERS),
          MarketDataSubscription.create(
              TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.TRADES),
          MarketDataSubscription.create(
              TickerSpec.fromKey("simulated/USD/BTC"), MarketDataType.USER_TRADE));

  private SubscriptionPublisher publisher = new SubscriptionPublisher();
  private SubscriptionControllerRemoteImpl controller =
      new SubscriptionControllerRemoteImpl(
          publisher, new RemoteMarketDataConfiguration("ws://localhost:8080/ws"));

  private CountDownLatch gotTickers = new CountDownLatch(3);
  private CountDownLatch gotAll = new CountDownLatch(subscriptions.size());
  private Set<MarketDataType> got = Sets.newConcurrentHashSet();

  @Before
  public void setup() {
    publisher
        .getTickers()
        .map(t -> MarketDataType.TICKER)
        .subscribe(
            type -> {
              received(type);
              gotTickers.countDown();
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
      client =
          new JerseyClientBuilder(SUPPORT.getEnvironment())
              .using(SUPPORT.getConfiguration().getJerseyClientConfiguration())
              .build("test client");
      controller.start();
      try {
        controller.updateSubscriptions(subscriptions);
        assertThat(waitForBalance("USD"), comparesEqualTo(new BigDecimal("200000")));
        waitUntilHadAFewTickers();
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
        assertThat(waitForBalance("USD"), comparesEqualTo(new BigDecimal("200000")));
        waitUntilHadAFewTickers();
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

  private void waitUntilHadAFewTickers() throws InterruptedException {
    Assert.assertTrue(gotTickers.await(1, TimeUnit.MINUTES));
  }

  private BigDecimal waitForBalance(String currency) {
    return publisher
        .getBalances()
        .map(it -> it.balance())
        .doOnNext(
            balanceEvent ->
                LOGGER.info(
                    "Balance received for {}: {}",
                    balanceEvent.getCurrency(),
                    balanceEvent.getTotal()))
        .filter(it -> it.getCurrency().getCurrencyCode().equals(currency))
        .limit(2)
        .skip(1) // The first balance we get after an action is likely to be stale.
        .timeout(30, TimeUnit.SECONDS)
        .map(it -> it.getTotal())
        .blockingFirst();
  }

  private void performTrade() {
    var order = new ExchangeResource.OrderPrototype();
    order.setAmount(new BigDecimal("0.01"));
    order.setBase("BTC");
    order.setCounter("USD");
    order.setLimitPrice(new BigDecimal(50_000));
    order.setType(BID);
    var response =
        client
            .target(
                String.format(
                    "http://localhost:%d/exchanges/simulated/orders", SUPPORT.getLocalPort()))
            .request()
            .post(Entity.json(order));
    assertEquals("Error: " + response.getEntity().toString(), 200, response.getStatus());
  }

  private void confirmAllDataTypesReceived() throws InterruptedException {
    LOGGER.info("Waiting for receipt");
    Assert.assertTrue(gotAll.await(1, TimeUnit.MINUTES));
  }
}
