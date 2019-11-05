package com.gruelbox.orko.exchange;

import javax.validation.constraints.NotNull;

public final class RemoteMarketDataConfiguration {

  @NotNull
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
