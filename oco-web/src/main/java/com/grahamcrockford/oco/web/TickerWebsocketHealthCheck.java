package com.grahamcrockford.oco.web;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.grahamcrockford.oco.spi.TickerSpec;

public class TickerWebsocketHealthCheck extends HealthCheck {

  private static final Logger LOGGER = LoggerFactory.getLogger(TickerWebsocketHealthCheck.class);

  private final ObjectMapper objectMapper;

  @Inject
  TickerWebsocketHealthCheck(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected Result check() throws Exception {
    ResultBuilder result = Result.builder().healthy();
    try {

      URI uri = new URI("ws://localhost:8080/api/ticker-ws"); // TODO

      result.withDetail("uri", uri);

      AtomicInteger bitfinexTickersReceived = new AtomicInteger();
      AtomicInteger gdaxTickersReceived = new AtomicInteger();
      AtomicInteger binanceTickersReceived = new AtomicInteger();
      AtomicInteger unknownTickersReceived = new AtomicInteger();

      try (TickerWebsocketClient clientEndPoint = new TickerWebsocketClient(uri, objectMapper, event -> {
        @SuppressWarnings("unchecked")
        Map<String, Object> spec = (Map<String, Object>) event.get("spec");
        switch ((String)spec.get("exchange")) {
          case "binance":
            binanceTickersReceived.incrementAndGet();
            break;
          case "gdax":
            gdaxTickersReceived.incrementAndGet();
            break;
          case "bitfinex":
            bitfinexTickersReceived.incrementAndGet();
            break;
          default:
            unknownTickersReceived.incrementAndGet();
            break;
        }
      })) {
        clientEndPoint.addTicker(TickerSpec.builder().exchange("bitfinex").counter("USD").base("BTC").build());
        clientEndPoint.addTicker(TickerSpec.builder().exchange("gdax").counter("USD").base("BTC").build());
        clientEndPoint.addTicker(TickerSpec.builder().exchange("binance").counter("USDT").base("BTC").build());
        Thread.sleep(30000);
      }

      return result
          .withDetail("bitfinexTickersReceived", bitfinexTickersReceived.get())
          .withDetail("gdaxTickersReceived", gdaxTickersReceived.get())
          .withDetail("binanceTickersReceived", binanceTickersReceived.get())
          .build();

    } catch (Throwable ex) {
      LOGGER.error("Error in healthcheck", ex);
      return result.withDetail("errorDescription", ex.getClass().getSimpleName() + ": " + ex.getMessage()).unhealthy(ex).build();
    }
  }
}