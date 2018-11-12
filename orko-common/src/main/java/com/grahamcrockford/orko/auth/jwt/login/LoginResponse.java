package com.grahamcrockford.orko.auth.jwt.login;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {

  @JsonProperty("success")
  private final boolean success;

  @JsonProperty("expiryMinutes")
  private final Integer expiryMinutes;

  public LoginResponse(int expiryMinutes) {
    this.expiryMinutes = expiryMinutes;
    this.success = true;
  }

  public LoginResponse() {
    this.expiryMinutes = null;
    this.success = false;
  }
}