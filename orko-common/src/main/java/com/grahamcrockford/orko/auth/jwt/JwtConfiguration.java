package com.grahamcrockford.orko.auth.jwt;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

public class JwtConfiguration {

  private String secret;
  private String userName;
  private String password;
  private String secondFactorSecret;
  private int expirationMinutes = 60 * 24;

  @JsonProperty
  String getSecret() {
    return secret;
  }

  @JsonProperty
  void setSecret(String secret) {
    this.secret = secret;
  }

  byte[] getSecretBytes() {
    Preconditions.checkNotNull(secret);
    return secret.getBytes(Charsets.UTF_8);
  }

  @JsonProperty
  String getUserName() {
    return userName;
  }

  @JsonProperty
  void setUserName(String userName) {
    this.userName = userName;
  }

  @JsonProperty
  String getPassword() {
    return password;
  }

  @JsonProperty
  void setPassword(String password) {
    this.password = password;
  }

  @JsonProperty
  public int getExpirationMinutes() {
    return expirationMinutes;
  }

  @JsonProperty
  public void setExpirationMinutes(int expirationMinutes) {
    this.expirationMinutes = expirationMinutes;
  }

  @JsonProperty
  public String getSecondFactorSecret() {
    return secondFactorSecret;
  }

  @JsonProperty
  public void setSecondFactorSecret(String secondFactorSecret) {
    this.secondFactorSecret = secondFactorSecret;
  }

  public boolean isEnabled() {
    return StringUtils.isNotEmpty(secret);
  }
}