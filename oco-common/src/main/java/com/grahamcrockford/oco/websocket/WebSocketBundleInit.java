package com.grahamcrockford.oco.websocket;

import javax.websocket.server.ServerEndpointConfig;

import com.google.inject.Inject;
import com.google.inject.Injector;

import io.dropwizard.websockets.WebsocketBundle;

public class WebSocketBundleInit {

  private final Injector injector;

  @Inject
  WebSocketBundleInit(Injector injector) {
    this.injector = injector;
  }

  public void init(WebsocketBundle websocketBundle) {
    final ServerEndpointConfig config = ServerEndpointConfig.Builder
        .create(OcoWebSocketServer.class, "/ws")
        .build();
    config.getUserProperties().put(Injector.class.getName(), injector);
    websocketBundle.addEndpoint(config);
  }
}
