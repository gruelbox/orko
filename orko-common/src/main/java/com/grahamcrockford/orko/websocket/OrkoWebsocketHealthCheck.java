package com.grahamcrockford.orko.websocket;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.grahamcrockford.orko.spi.TickerSpec;

class OrkoWebsocketHealthCheck extends HealthCheck {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrkoWebsocketHealthCheck.class);

  private final ObjectMapper objectMapper;
  private final Provider<HttpServletRequest> request;

  @Inject
  OrkoWebsocketHealthCheck(ObjectMapper objectMapper, Provider<HttpServletRequest> request) {
    this.objectMapper = objectMapper;
    this.request = request;
  }

  @Override
  protected Result check() throws Exception {
    ResultBuilder result = Result.builder().healthy();
    try {

      String header = request.get().getHeader("authorization");
      if (header == null || !header.startsWith("Bearer ") || header.length() <= 7) {
        return result.withMessage("Requires access token").unhealthy().build();
      }
      String accessToken = header.substring(7);

      URI uri = new URI("ws://X-Bearer:" + accessToken + "@" +
                        request.get().getServerName() +
                        "." +
                        request.get().getServerPort() +
                        request.get().getContextPath() +
                        "/ws");

      result.withDetail("uri", uri);

      AtomicInteger bitfinexTickersReceived = new AtomicInteger();
      AtomicInteger gdaxTickersReceived = new AtomicInteger();
      AtomicInteger binanceTickersReceived = new AtomicInteger();
      AtomicInteger unknownTickersReceived = new AtomicInteger();

      try (OrkoWebsocketClient clientEndPoint = new OrkoWebsocketClient(uri, objectMapper, event -> {
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
        clientEndPoint.changeTickers(
          ImmutableList.of(
            TickerSpec.builder().exchange("bitfinex").counter("USD").base("BTC").build(),
            TickerSpec.builder().exchange("gdax").counter("USD").base("BTC").build(),
            TickerSpec.builder().exchange("binance").counter("USDT").base("BTC").build()
          )
        );
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