package com.grahamcrockford.orko.auth.jwt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.auth.basic.BasicCredentials;

public class LoginRequest extends BasicCredentials {
  @JsonCreator
  public LoginRequest(@JsonProperty("username") String username, @JsonProperty("password") String password) {
    super(username, password);
  }
}