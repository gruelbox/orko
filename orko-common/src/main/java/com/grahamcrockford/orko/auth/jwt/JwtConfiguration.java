package com.grahamcrockford.orko.auth.jwt;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

public class JwtConfiguration {

  @JsonProperty private String secret;
  @JsonProperty private String userName;
  @JsonProperty private String password;
  @JsonProperty private String secondFactorSecret;
  @JsonProperty private int expirationMinutes = 60 * 24;
  
  String getSecret() {
    return secret;
  }

  void setSecret(String secret) {
    this.secret = secret;
  }

  public byte[] getSecretBytes() {
    Preconditions.checkNotNull(secret);
    return secret.getBytes(Charsets.UTF_8);
  }

  public String getUserName() {
    return userName;
  }
  
  void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  void setPassword(String password) {
    this.password = password;
  }

  public int getExpirationMinutes() {
    return expirationMinutes;
  }

  void setExpirationMinutes(int expirationMinutes) {
    this.expirationMinutes = expirationMinutes;
  }
  
  public String getSecondFactorSecret() {
    return secondFactorSecret;
  }

  void setSecondFactorSecret(String secondFactorSecret) {
    this.secondFactorSecret = secondFactorSecret;
  }

  public boolean isEnabled() {
    return StringUtils.isNotEmpty(secret);
  }
}