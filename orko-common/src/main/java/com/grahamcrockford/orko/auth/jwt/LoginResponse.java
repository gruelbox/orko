package com.grahamcrockford.orko.auth.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {

  @JsonProperty("token")
  public String token;

  @JsonProperty("success")
	private final boolean success;

  public LoginResponse(String token) {
		this.token = token;
		this.success = true;
	}

	public LoginResponse() {
    this.token = null;
    this.success = false;
  }

}