package com.grahamcrockford.orko.auth;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OktaConfiguration {

  @NotNull
  private String baseUrl;

  @NotNull
  private String clientId;

  @NotNull
  private String issuer;

  private String audience;

  @JsonProperty
  public String getBaseUrl() {
    return baseUrl;
  }

  @JsonProperty
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  @JsonProperty
  public String getClientId() {
    return clientId;
  }

  @JsonProperty
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  @JsonProperty
  public String getIssuer() {
    return issuer;
  }

  @JsonProperty
  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  @JsonProperty
  public String getAudience() {
    return audience;
  }

  @JsonProperty
  public void setAudience(String audience) {
    this.audience = audience;
  }
}