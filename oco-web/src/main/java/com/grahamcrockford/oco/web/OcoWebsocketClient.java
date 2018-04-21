package com.grahamcrockford.oco.web;

import static com.grahamcrockford.oco.web.OcoWebSocketIncomingMessage.Command.START_TICKER;
import static com.grahamcrockford.oco.web.OcoWebSocketIncomingMessage.Command.STOP_TICKER;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.web.OcoWebSocketIncomingMessage.Command;

@ClientEndpoint
public class OcoWebsocketClient implements AutoCloseable {

  private final Consumer<Map<String, Object>> consumer;
  private final ObjectMapper objectMapper;

  private Session session;


  public OcoWebsocketClient(URI endpointURI,
                            ObjectMapper objectMapper,
                            Consumer<Map<String, Object>> consumer) {
    this.objectMapper = objectMapper;
    this.consumer = consumer;
    try {
      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      container.connectToServer(this, endpointURI);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
  }

  @SuppressWarnings({ "unchecked" })
  @OnMessage
  public void onMessage(String message, Session session) throws JsonParseException, JsonMappingException, IOException {
    consumer.accept(objectMapper.readValue(message, Map.class));
  }

  public void addTicker(TickerSpec spec) {
    sendCommand(START_TICKER, spec);
  }

  public void removeTicker(TickerSpec spec) {
    sendCommand(STOP_TICKER, spec);
  }

  private void sendCommand(Command command, TickerSpec spec) {
    try {
      OcoWebSocketIncomingMessage request = OcoWebSocketIncomingMessage.create(command, UUID.randomUUID().toString(), spec);
      String message = objectMapper.writeValueAsString(request);
      this.session.getAsyncRemote().sendText(message);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() throws IOException {
    if (this.session != null) {
      this.session.close();
    }
  }
}