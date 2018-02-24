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
}