package com.gruelbox.orko.exchange;

public final class RemoteMarketDataConfiguration {

  private String webSocketUri;
  private String exchangeEndpointUri;

  public RemoteMarketDataConfiguration() {}

  public RemoteMarketDataConfiguration(String webSocketUri) {
    this.webSocketUri = webSocketUri;
  }

  public String getWebSocketUri() {
    return webSocketUri;
  }

  public void setWebSocketUri(String webSocketUri) {
    this.webSocketUri = webSocketUri;
  }

  public String getExchangeEndpointUri() {
    return exchangeEndpointUri;
  }

  public void setExchangeEndpointUri(String exchangeEndpointUri) {
    this.exchangeEndpointUri = exchangeEndpointUri;
  }
}
