package com.gruelbox.orko.auth;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gruelbox.orko.auth.ipwhitelisting.IpWhitelistingConfiguration;
import com.gruelbox.orko.auth.jwt.JwtConfiguration;
import com.gruelbox.orko.auth.okta.OktaConfiguration;

public class AuthConfiguration {

  @JsonProperty private OktaConfiguration okta;
  @JsonProperty private JwtConfiguration jwt;
  @JsonProperty private IpWhitelistingConfiguration ipWhitelisting;
  
  /**
   * Set to {@code true} on Heroku so it uses the `X-Forwarded-For` header to
   * determine the source IP. This MUST be {@code false} if you're not hosted behind a
   * trusted proxy where you can 100% believe the `X-Forwarded-For` header, or
   * someone could easily spoof their IP and bypass your 2FA.
   */
  @NotNull @JsonProperty private boolean proxied;
  @JsonProperty private boolean httpsOnly;
  @JsonProperty private int attemptsBeforeBlacklisting = 10;
  @JsonProperty private int blacklistingExpirySeconds = 600;

  private String authCachePolicy = "maximumSize=10000, expireAfterAccess=10m";

  public OktaConfiguration getOkta() {
    return okta;
  }

  void setOkta(OktaConfiguration okta) {
    this.okta = okta;
  }

  public boolean isProxied() {
    return proxied;
  }

  public void setProxied(boolean proxied) {
    this.proxied = proxied;
  }

  
  public boolean isHttpsOnly() {
    return httpsOnly;
  }

  void setHttpsOnly(boolean httpsOnly) {
    this.httpsOnly = httpsOnly;
  }

  public String getAuthCachePolicy() {
    return authCachePolicy;
  }

  void setAuthCachePolicy(String authCachePolicy) {
    this.authCachePolicy = authCachePolicy;
  }

  public JwtConfiguration getJwt() {
    return jwt;
  }

  public void setJwt(JwtConfiguration jwt) {
    this.jwt = jwt;
  }

  public IpWhitelistingConfiguration getIpWhitelisting() {
    return ipWhitelisting;
  }

  public void setIpWhitelisting(IpWhitelistingConfiguration ipWhitelisting) {
    this.ipWhitelisting = ipWhitelisting;
  }
  
  public int getAttemptsBeforeBlacklisting() {
    return attemptsBeforeBlacklisting;
  }

  public void setAttemptsBeforeBlacklisting(int attemptsBeforeBlacklisting) {
    this.attemptsBeforeBlacklisting = attemptsBeforeBlacklisting;
  }

  public int getBlacklistingExpirySeconds() {
    return blacklistingExpirySeconds;
  }

  public void setBlacklistingExpirySeconds(int blacklistingExpirySeconds) {
    this.blacklistingExpirySeconds = blacklistingExpirySeconds;
  }
}