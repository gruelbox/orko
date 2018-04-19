package com.grahamcrockford.oco.web;

import java.io.IOException;
import java.security.Principal;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.security.RolesAllowed;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
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
import com.grahamcrockford.oco.auth.Roles;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.ticker.ExchangeEventRegistry;
import com.grahamcrockford.oco.ticker.TickerEvent;

@Metered
@Timed
@ExceptionMetered
@ServerEndpoint("/api/ticker-ws")
@RolesAllowed(Roles.TRADER)
public final class TickerWebSocketServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(TickerWebSocketServer.class);

  private final String uuid = UUID.randomUUID().toString();
  private final ConcurrentMap<TickerSpec, Boolean> registeredTickers = Maps.newConcurrentMap();

  @Inject private ExchangeEventRegistry exchangeEventRegistry;
  @Inject private ObjectMapper objectMapper;

  @OnOpen
  public void myOnOpen(final javax.websocket.Session session) throws IOException, InterruptedException {
    LOGGER.info("Opening socket");
    Principal principal = (Principal)session.getUserProperties().get("user");
    if (principal == null) {
      session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "Authentication violation"));
    }
    injectMembers(session);
  }

  @OnMessage
  public void myOnMsg(final javax.websocket.Session session, String message) {
    TickerWebSocketRequest request;
    try {
      request = objectMapper.readValue(message, TickerWebSocketRequest.class);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid request", e);
    }

    if (request.command().equals(TickerWebSocketRequest.Command.START)) {
      if (registeredTickers.putIfAbsent(request.ticker(), false) == null) {
        exchangeEventRegistry.registerTicker(request.ticker(), uuid, ticker -> tick(session, request.ticker(), ticker));
      }
    } else if (request.command().equals(TickerWebSocketRequest.Command.STOP)) {
      if (registeredTickers.remove(request.ticker()) != null) {
        exchangeEventRegistry.unregisterTicker(request.ticker(), uuid);
      }
    } else {
      // Jackson should stop this happening in the try block above, but just for completeness
      throw new IllegalArgumentException("Invalid command: " + request.command());
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