package com.gruelbox.orko.exchange;

import org.apache.commons.lang3.StringUtils;

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

  public boolean isEnabled() {
    return StringUtils.isNotBlank(webSocketUri) &&
        StringUtils.isNotBlank(exchangeEndpointUri);
  }

  public String getExchangeEndpointUri() {
    return exchangeEndpointUri;
  }

  public void setExchangeEndpointUri(String exchangeEndpointUri) {
    this.exchangeEndpointUri = exchangeEndpointUri;
  }
}
