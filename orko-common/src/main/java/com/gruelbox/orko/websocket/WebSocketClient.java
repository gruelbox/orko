package com.gruelbox.orko.websocket;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Emitter;

/**
 * Simple Rx Websocket client.
 */
@ClientEndpoint
public final class WebSocketClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClient.class);

  Session session;
  private Emitter<String> emitter;

  public WebSocketClient(Emitter<String> emitter) {
    this.emitter = emitter;
  }

  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
    LOGGER.info("Connected to {}", session.getRequestURI());
  }

  @OnClose
  public void onClose(Session session, CloseReason reason) {
    LOGGER.info("Connection to {} closed:", session.getRequestURI(), reason);
    this.session = null;
    if (reason.getCloseCode() != CloseReason.CloseCodes.NORMAL_CLOSURE) {
      emitter.onError(new AbnormalClosureException(reason));
    }
  }

  @OnMessage
  public void onMessage(String message) {
    emitter.onNext(message);
  }

  @OnError
  public void onError(Throwable t) {
    emitter.onError(t);
  }

  static final class AbnormalClosureException extends RuntimeException {

    private final CloseReason reason;

    private AbnormalClosureException(CloseReason reason) {
      super("Abnormal socket closure: " + reason);
      this.reason = reason;
    }

    CloseReason getReason() {
      return reason;
    }
  }
}