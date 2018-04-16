package com.grahamcrockford.oco.web.service;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grahamcrockford.oco.api.ticker.TickerEvent;
import com.grahamcrockford.oco.spi.TickerSpec;

@ClientEndpoint
public class TickerWebsocketClient implements AutoCloseable {

  private final CountDownLatch ready = new CountDownLatch(1);
  private final Consumer<TickerEvent> consumer;
  private final ObjectMapper objectMapper;

  private Session session;

  public TickerWebsocketClient(URI endpointURI,
                               ObjectMapper objectMapper,
                               Consumer<TickerEvent> consumer) {
    this.objectMapper = objectMapper;
    this.consumer = consumer;
    try {
      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      container.connectToServer(this, endpointURI);
      if (!ready.await(10, TimeUnit.SECONDS)) {
        throw new TimeoutException("Failed to receive handshake within timeout");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @OnMessage
  public void onMessage(String message, Session session) throws JsonParseException, JsonMappingException, IOException {
    if ("HELLO".equals(message)) {
      this.session = session;
      ready.countDown();
    } else {
      consumer.accept(objectMapper.readValue(message, TickerEvent.class));
    }
  }

  public void addTicker(TickerSpec spec) {
    this.session.getAsyncRemote().sendText("START/" + spec.exchange() + "/" + spec.counter() + "/" + spec.base());
  }

  public void removeTicker(TickerSpec spec) {
    this.session.getAsyncRemote().sendText("STOP/" + spec.exchange() + "/" + spec.counter() + "/" + spec.base());
  }

  @Override
  public void close() throws IOException {
    if (this.session != null) {
      this.session.close();
    }
  }
}