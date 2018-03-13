package com.grahamcrockford.oco.auth;

import io.dropwizard.Configuration;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthConfiguration extends Configuration {

  @NotNull
  private String userName;

  @NotNull
  private String password;

  private String secretKey;

  private Integer whitelistExpirySeconds;

  private boolean cors = true;

  @NotNull
  private boolean proxied;

  public AuthConfiguration() {
    super();
  }

  @JsonProperty
  public String getUserName() {
    return userName;
  }

  @JsonProperty
  public void setUserName(String userName) {
    this.userName = userName;
  }

  @JsonProperty
  public String getPassword() {
    return password;
  }

  @JsonProperty
  public void setPassword(String password) {
    this.password = password;
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
  public boolean isProxied() {
    return proxied;
  }

  @JsonProperty
  public void setProxied(boolean proxied) {
    this.proxied = proxied;
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
  public boolean isCors() {
    return cors;
  }

  @JsonProperty
  public void setCors(boolean cors) {
    this.cors = cors;
  }
}