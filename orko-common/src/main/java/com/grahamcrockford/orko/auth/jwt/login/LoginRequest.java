package com.grahamcrockford.orko.auth.jwt.login;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequest {

  private final String username;
  private final String password;
  private final Integer secondFactor;

  @JsonCreator
  public LoginRequest(@JsonProperty("username") String username,
                      @JsonProperty("password") String password,
                      @JsonProperty("secondFactor") Integer secondFactor) {
    this.secondFactor = secondFactor;
    this.username = requireNonNull(username);
    this.password = requireNonNull(password);
  }

  @JsonProperty
  public String getUsername() {
      return username;
  }

  @JsonProperty
  public String getPassword() {
      return password;
  }

  @JsonProperty
  public Integer getSecondFactor() {
    return secondFactor;
  }
}