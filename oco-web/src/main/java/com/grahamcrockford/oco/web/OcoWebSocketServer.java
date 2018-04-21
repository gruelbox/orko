package com.grahamcrockford.oco.web;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.security.RolesAllowed;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

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
import com.grahamcrockford.oco.web.OcoWebSocketOutgoingMessage.Nature;

@Metered
@Timed
@ExceptionMetered
@ServerEndpoint("/api/ws")
@RolesAllowed(Roles.TRADER)
public final class OcoWebSocketServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(OcoWebSocketServer.class);

  private final String uuid = UUID.randomUUID().toString();
  private final ConcurrentMap<TickerSpec, Boolean> registeredTickers = Maps.newConcurrentMap();

  @Inject private ExchangeEventRegistry exchangeEventRegistry;
  @Inject private ObjectMapper objectMapper;

  @OnOpen
  public void myOnOpen(final javax.websocket.Session session) throws IOException, InterruptedException {
    LOGGER.info("Opening socket");
    injectMembers(session);
  }

  @OnMessage
  public void myOnMsg(final javax.websocket.Session session, String message) {
    OcoWebSocketIncomingMessage request = null;
    try {

      request = decodeRequest(message);

      switch (request.command()) {
        case START_TICKER:
          startTicker(request.ticker(), session);
          break;
        case STOP_TICKER:
          stopTicker(request.ticker());
          break;
        default:
          // Jackson should stop this happening in the try block above, but just for completeness
          throw new IllegalArgumentException("Invalid command: " + request.command());
      }

    } catch (Exception e) {
      LOGGER.error("Error processing message: " + message, e);
      session.getAsyncRemote().sendText(message(Nature.ERROR, request == null ? null : request.correlationId(), "Error processing message"));
      return;
    }
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

  private OcoWebSocketIncomingMessage decodeRequest(String message) {
    OcoWebSocketIncomingMessage request;
    try {
      request = objectMapper.readValue(message, OcoWebSocketIncomingMessage.class);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid request", e);
    }
    return request;
  }


  private void startTicker(TickerSpec spec, Session session) {
    if (registeredTickers.putIfAbsent(spec, false) == null) {
      exchangeEventRegistry.registerTicker(spec, uuid, t -> {
        LOGGER.debug("Tick: {}", t);
        session.getAsyncRemote().sendText(message(Nature.TICKER, null, TickerEvent.create(spec, t)));
      });
    }
  }

  private void stopTicker(TickerSpec spec) {
    if (registeredTickers.remove(spec) != null) {
      exchangeEventRegistry.unregisterTicker(spec, uuid);
    }
  }

  private String message(Nature nature, String correlationId, Object data) {
    try {
      return objectMapper.writeValueAsString(OcoWebSocketOutgoingMessage.create(nature, correlationId, data));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}