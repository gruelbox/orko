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
package com.gruelbox.orko.integration;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.knowm.xchange.dto.Order.OrderType.BID;

import ch.qos.logback.classic.Level;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.collect.Sets;
import com.gruelbox.orko.app.marketdata.MarketDataApplication;
import com.gruelbox.orko.app.monolith.MonolithApplication;
import com.gruelbox.orko.exchange.ExchangeResource;
import com.gruelbox.orko.exchange.MarketDataSubscription;
import com.gruelbox.orko.exchange.MarketDataType;
import com.gruelbox.orko.exchange.RemoteMarketDataConfiguration;
import com.gruelbox.orko.exchange.SubscriptionControllerRemoteImpl;
import com.gruelbox.orko.exchange.SubscriptionPublisher;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.Safely;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.validation.ValidatorFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chains together the market data and primary service via websockets and confirms we get data
 * through both.
 */
public class TestAllServices {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestAllServices.class);

  private Process marketDataProcess;
  private Process mainProcess;
  private Future<?> marketDataProcessOutput;
  private Future<?> mainProcessOutput;
  private Future<?> marketDataProcessInput;
  private Future<?> mainProcessInput;

  private Client client;
  private ValidatorFactory validatorFactory;

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

  private ExecutorService executorService;

  @Before
  public void setup() throws Exception {
    BootstrapLogging.bootstrap(Level.INFO);
    executorService = Executors.newFixedThreadPool(8);

    LOGGER.info("Starting market data service...");
    marketDataProcess =
        Fork.exec(
            MarketDataApplication.class, "server", resourceFilePath("marketdata-test-config.yml"));
    marketDataProcessInput = Fork.keepAlive(marketDataProcess, executorService);
    marketDataProcessOutput = Fork.pipeOutput(marketDataProcess, System.out, executorService);

    LOGGER.info("Starting main service...");
    mainProcess =
        Fork.exec(MonolithApplication.class, "server", resourceFilePath("main-test-config.yml"));
    mainProcessInput = Fork.keepAlive(mainProcess, executorService);
    mainProcessOutput = Fork.pipeOutput(mainProcess, System.out, executorService);

    LOGGER.info("Connecting subscribers...");
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

    LOGGER.info("Creating client...");
    validatorFactory = Validators.newValidatorFactory();
    client = createJerseyClient();

    LOGGER.info("Connecting to websocket...");
    controller.start();
  }

  private Client createJerseyClient() {
    Environment environment =
        new Environment(
            "Main app",
            StreamingObjectMapperHelper.getObjectMapper(),
            validatorFactory,
            new MetricRegistry(),
            null,
            new HealthCheckRegistry(),
            new Configuration());

    JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
    jerseyClientConfiguration.setTimeout(Duration.seconds(30));
    jerseyClientConfiguration.setConnectionTimeout(Duration.seconds(30));
    jerseyClientConfiguration.setConnectionRequestTimeout(Duration.seconds(30));
    return new JerseyClientBuilder(environment)
        .using(jerseyClientConfiguration)
        .build("Test client");
  }

  @After
  public void tearDown() throws Exception {
    LOGGER.info("Stopping controller...");
    if (controller != null) Safely.run("stopping controller", controller::stop);
    LOGGER.info("Closing client...");
    if (client != null) Safely.run("closing client", client::close);
    LOGGER.info("Closing validator factory...");
    if (validatorFactory != null) Safely.run("closing validator factory", validatorFactory::close);
    LOGGER.info("Stopping main app...");
    stopFuture("stopping main process output pipe", mainProcessOutput);
    stopFuture("stopping main process input pipe", mainProcessInput);
    if (mainProcess != null) shutdown("stopping main app gracefully", mainProcess);
    LOGGER.info("Stopping market data app...");
    stopFuture("stopping market data process pipe", marketDataProcessOutput);
    stopFuture("stopping main process input pipe", marketDataProcessInput);
    if (marketDataProcess != null)
      shutdown("stopping market data app gracefully", marketDataProcess);
    executorService.shutdown();
    executorService.awaitTermination(30, TimeUnit.SECONDS);
  }

  private void stopFuture(String s, Future<?> mainProcessInput) {
    Safely.run(s, () -> mainProcessInput.cancel(true));
  }

  private void shutdown(String description, Process process) throws InterruptedException {
    Safely.run(description, process::destroy);
    if (!process.waitFor(1, TimeUnit.MINUTES)) {
      Safely.run("stopping main app gracefully", process::destroyForcibly);
    }
  }

  @Test
  public void testWebSockets() {
    try {
      controller.updateSubscriptions(subscriptions);
      assertThat(waitForBalance("USD"), comparesEqualTo(new BigDecimal("200000")));
      waitUntilHadAFewTickers();
      performTrade();
      confirmAllDataTypesReceived();
      assertThat(waitForBalance("BTC"), comparesEqualTo(new BigDecimal("0.01")));
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
        .timeout(1, TimeUnit.MINUTES)
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
            .target("http://localhost:8080/main/exchanges/simulated/orders")
            .request()
            .post(Entity.json(order));
    assertEquals("Error: " + response.getEntity().toString(), 200, response.getStatus());
    LimitOrder result = response.readEntity(LimitOrder.class);
    LOGGER.info("Order posted : {}", result);
    assertEquals(order.getAmount(), result.getOriginalAmount());
    assertEquals(order.getLimitPrice(), result.getLimitPrice());
    assertEquals(order.getType(), result.getType());
    assertThat(result.getId(), not(emptyString()));
  }

  private void confirmAllDataTypesReceived() throws InterruptedException {
    LOGGER.info("Waiting for receipt");
    Assert.assertTrue(gotAll.await(1, TimeUnit.MINUTES));
  }
}
