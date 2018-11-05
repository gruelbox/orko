package com.grahamcrockford.orko.auth;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grahamcrockford.orko.auth.ipwhitelisting.IpWhitelistingConfiguration;
import com.grahamcrockford.orko.auth.jwt.JwtConfiguration;
import com.grahamcrockford.orko.auth.okta.OktaConfiguration;

public class AuthConfiguration {

  private OktaConfiguration okta;
  private JwtConfiguration jwt;
  private IpWhitelistingConfiguration ipWhitelisting;

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
  public boolean isProxied() {
    return proxied;
  }

  @JsonProperty
  void setProxied(boolean proxied) {
    this.proxied = proxied;
  }

  @JsonProperty
  boolean isHttpsOnly() {
    return httpsOnly;
  }

  @JsonProperty
  void setHttpsOnly(boolean httpsOnly) {
    this.httpsOnly = httpsOnly;
  }

  @JsonProperty
  public String getAuthCachePolicy() {
    return authCachePolicy;
  }

  @JsonProperty
  void setAuthCachePolicy(String authCachePolicy) {
    this.authCachePolicy = authCachePolicy;
  }

  @JsonProperty
  public JwtConfiguration getJwt() {
    return jwt;
  }

  @JsonProperty
  public void setJwt(JwtConfiguration jwt) {
    this.jwt = jwt;
  }

  @JsonProperty
  public IpWhitelistingConfiguration getIpWhitelisting() {
    return ipWhitelisting;
  }

  @JsonProperty
  public void setIpWhitelisting(IpWhitelistingConfiguration ipWhitelisting) {
    this.ipWhitelisting = ipWhitelisting;
  }
}