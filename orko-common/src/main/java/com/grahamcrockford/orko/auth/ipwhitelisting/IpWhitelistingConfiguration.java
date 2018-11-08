package com.grahamcrockford.orko.auth.ipwhitelisting;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IpWhitelistingConfiguration {

  private String secretKey;
  private int whitelistExpirySeconds = 28800;

  @JsonProperty
  String getSecretKey() {
    return secretKey;
  }

  @JsonProperty
  void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  @JsonProperty
  public Integer getWhitelistExpirySeconds() {
    return whitelistExpirySeconds;
  }

  @JsonProperty
  public void setWhitelistExpirySeconds(Integer whitelistExpirySeconds) {
    this.whitelistExpirySeconds = whitelistExpirySeconds;
  }

  public boolean isEnabled() {
    return  StringUtils.isNotEmpty(secretKey);
  }
}