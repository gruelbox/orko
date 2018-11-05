package com.grahamcrockford.orko.auth.okta;

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
  String getBaseUrl() {
    return baseUrl;
  }

  @JsonProperty
  void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  @JsonProperty
  String getClientId() {
    return clientId;
  }

  @JsonProperty
  void setClientId(String clientId) {
    this.clientId = clientId;
  }

  @JsonProperty
  String getIssuer() {
    return issuer;
  }

  @JsonProperty
  void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  @JsonProperty
  String getAudience() {
    return audience;
  }

  @JsonProperty
  void setAudience(String audience) {
    this.audience = audience;
  }
}