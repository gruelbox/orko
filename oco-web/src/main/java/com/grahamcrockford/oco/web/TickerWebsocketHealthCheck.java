package com.grahamcrockford.oco.web;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.grahamcrockford.oco.spi.TickerSpec;

public class TickerWebsocketHealthCheck extends HealthCheck {

  private final ObjectMapper objectMapper;

  @Inject
  TickerWebsocketHealthCheck(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected Result check() throws Exception {
    ResultBuilder result = Result.builder();
    try {

      URI uri = new URI("ws://localhost:8080/api/ticker-ws");

      result.withDetail("uri", uri);

      AtomicInteger tickersReceived = new AtomicInteger();

      try (TickerWebsocketClient clientEndPoint = new TickerWebsocketClient(uri, objectMapper, event -> {
        System.out.println(event);
        tickersReceived.incrementAndGet();
      })) {
        clientEndPoint.addTicker(TickerSpec.builder().exchange("bitfinex").counter("USD").base("BTC").build());
//        clientEndPoint.addTicker(TickerSpec.builder().exchange("gdax").counter("USD").base("EUR").build());
//        clientEndPoint.addTicker(TickerSpec.builder().exchange("binance").counter("USDT").base("BTC").build());
        Thread.sleep(30000);
      }

      return result.withDetail("tickersReceived", tickersReceived.get()).healthy().build();

    } catch (Throwable ex) {
      return result.unhealthy(ex).build();
    }
  }
}