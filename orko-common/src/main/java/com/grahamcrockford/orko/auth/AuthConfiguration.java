package com.grahamcrockford.orko.auth;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grahamcrockford.orko.auth.okta.OktaConfiguration;

public class AuthConfiguration {

  /**
   * Okta configuration.
   */
  private OktaConfiguration okta;

  /**
   * If set, is the 2FA secret key for IP whitelisting. If disabled, so is IP
   * whitelisting.
   */
  private String secretKey;

  /**
   * How long whitelisting should live for.
   */
  private int whitelistExpirySeconds = 28800;

  /**
   * Set to {@code true} on Heroku so it uses the `X-Forwarded-For` header to
   * determine the source IP. This MUST be {@code false} if you're not hosted behind a
   * trusted proxy where you can 100% believe the `X-Forwarded-For` header, or
   * someone could easily spoof their IP and bypass your 2FA.
   */
  @NotNull
  private boolean proxied;

  private boolean httpsOnly;

  private String authCachePolicy = "maximumSize=10000, expireAfterAccess=10m";

  public OktaConfiguration getOkta() {
    return okta;
  }

  @JsonProperty
  public void setOkta(OktaConfiguration okta) {
    this.okta = okta;
  }

  @JsonProperty
  public String getSecretKey() {
    return secretKey;
  }

  @JsonProperty
  public void setSecretKey(String secretKey) {
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

  @JsonProperty
  public boolean isProxied() {
    return proxied;
  }

  @JsonProperty
  public void setProxied(boolean proxied) {
    this.proxied = proxied;
  }

  @JsonProperty
  public boolean isHttpsOnly() {
    return httpsOnly;
  }

  @JsonProperty
  public void setHttpsOnly(boolean httpsOnly) {
    this.httpsOnly = httpsOnly;
  }

  @JsonProperty
  public String getAuthCachePolicy() {
    return authCachePolicy;
  }

  @JsonProperty
  public void setAuthCachePolicy(String authCachePolicy) {
    this.authCachePolicy = authCachePolicy;
  }
}