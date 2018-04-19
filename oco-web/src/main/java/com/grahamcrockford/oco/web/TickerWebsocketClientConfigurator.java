package com.grahamcrockford.oco.web;

import java.util.List;
import java.util.Map;

import javax.websocket.ClientEndpointConfig.Configurator;

import com.google.common.collect.ImmutableList;

public class TickerWebsocketClientConfigurator extends Configurator {
  @Override
  public void beforeRequest(Map<String, List<String>> headers) {
    headers.put("authorization", ImmutableList.of("Bearer NOTAVALIDJWT"));
    super.beforeRequest(headers);
  }
}
