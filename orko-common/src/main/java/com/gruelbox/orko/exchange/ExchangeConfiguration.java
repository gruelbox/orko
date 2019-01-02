package com.gruelbox.orko.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExchangeConfiguration {

  private String userName;
  private String secretKey;
  private String apiKey;
  private String passphrase;

  @JsonProperty
  public String getUserName() {
    return userName;
  }

  @JsonProperty
  public String getSecretKey() {
    return secretKey;
  }

  @JsonProperty
  public String getApiKey() {
    return apiKey;
  }

  @JsonProperty
  public String getPassphrase() {
    return passphrase;
  }

  @JsonProperty
  public void setUserName(String userName) {
    this.userName = userName;
  }

  @JsonProperty
  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  @JsonProperty
  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  @JsonProperty
  public void setPassphrase(String passphrase) {
    this.passphrase = passphrase;
  }
}