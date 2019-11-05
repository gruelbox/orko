package com.gruelbox.orko.exchange;

public final class RemoteMarketDataConfiguration {

  private String remoteUri;

  public RemoteMarketDataConfiguration() {}

  public RemoteMarketDataConfiguration(String remoteUri) {
    this.remoteUri = remoteUri;
  }

  public String getRemoteUri() {
    return remoteUri;
  }

  public void setRemoteUri(String remoteUri) {
    this.remoteUri = remoteUri;
  }
}
