package com.grahamcrockford.oco.web;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;

import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.ticker.ExchangeEventRegistry;
import com.grahamcrockford.oco.ticker.TickerEvent;

@Metered
@Timed
@ExceptionMetered
@ServerEndpoint("/api/ticker-ws")
public final class TickerWebSocketServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(TickerWebSocketServer.class);

  private final String uuid = UUID.randomUUID().toString();
  private final ConcurrentMap<TickerSpec, Boolean> registeredTickers = Maps.newConcurrentMap();

  @Inject private ExchangeEventRegistry exchangeEventRegistry;
  @Inject private ObjectMapper objectMapper;

  @OnOpen
  public void myOnOpen(final javax.websocket.Session session) throws IOException, InterruptedException {
    injectMembers(session);
    session.getAsyncRemote().sendText("HELLO");
  }

  @OnMessage
  public void myOnMsg(final javax.websocket.Session session, String message) {
    String[] split = message.split("/");
    if (split.length != 4)
      throw new IllegalArgumentException("Invalid instruction: " + message);

    TickerSpec spec = TickerSpec.builder().exchange(split[1]).counter(split[2]).base(split[3]).build();

    if (split[0].equals("START")) {
      if (registeredTickers.putIfAbsent(spec, false) == null) {
        exchangeEventRegistry.registerTicker(spec, uuid, ticker -> tick(session, spec, ticker));
      }
    } else if (split[0].equals("STOP")) {
      if (registeredTickers.remove(spec) != null) {
        exchangeEventRegistry.unregisterTicker(spec, uuid);
      }
    } else {
      throw new IllegalArgumentException("Invalid verb: " + split[0]);
    }
  }

  private void tick(javax.websocket.Session session, TickerSpec spec, Ticker ticker) {
    LOGGER.debug("Tick: {}", ticker);
    String message;
    try {
      message = objectMapper.writeValueAsString(TickerEvent.create(spec, ticker));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    session.getAsyncRemote().sendText(message);
  }

  @OnClose
  public void myOnClose(final javax.websocket.Session session, CloseReason cr) {
    LOGGER.info("Closing socket ({})", cr.toString());
    registeredTickers.keySet().forEach(spec -> exchangeEventRegistry.unregisterTicker(spec, uuid));
  }

  @OnError
  public void onError(Throwable error) {
  }


  private void injectMembers(final javax.websocket.Session session) {
    Injector injector = (Injector) session.getUserProperties().get(Injector.class.getName());
    injector.injectMembers(this);
  }
}