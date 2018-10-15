package com.grahamcrockford.oco.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
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
import com.grahamcrockford.oco.websocket.OrkoWebSocketIncomingMessage.Command;

@ClientEndpoint
class OrkoWebsocketClient implements AutoCloseable {

  private final Consumer<Map<String, Object>> consumer;
  private final ObjectMapper objectMapper;

  private Session session;


  public OrkoWebsocketClient(URI endpointURI,
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

  public void changeTickers(Collection<TickerSpec> specs) {
    sendCommand(Command.CHANGE_TICKERS, specs);
  }

  private void sendCommand(Command command, Collection<TickerSpec> specs) {
    try {
      OrkoWebSocketIncomingMessage request = OrkoWebSocketIncomingMessage.create(command, specs);
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