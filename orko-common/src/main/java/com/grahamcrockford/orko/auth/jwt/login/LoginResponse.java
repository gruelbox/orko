package com.grahamcrockford.orko.auth.jwt.login;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {

  @JsonProperty("success")
  private final boolean success;

  @JsonProperty("expiryMinutes")
  private final Integer expiryMinutes;
  
  @JsonProperty("xsrf")
  private final String xsrf;

  public LoginResponse(int expiryMinutes, String xsrf) {
    this.expiryMinutes = expiryMinutes;
	this.xsrf = xsrf;
    this.success = true;
  }

  public LoginResponse() {
    this.expiryMinutes = null;
    this.success = false;
    this.xsrf = null;
  }
}