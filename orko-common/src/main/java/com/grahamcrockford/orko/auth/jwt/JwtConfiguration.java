package com.grahamcrockford.orko.auth.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

public class JwtConfiguration {

  private String secret;
  private String userName;
  private String password;

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
}