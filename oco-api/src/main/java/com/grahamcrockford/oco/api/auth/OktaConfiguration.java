package com.grahamcrockford.oco.api.auth;

import javax.validation.constraints.NotNull;

public class OktaConfiguration {

  @NotNull
  public String baseUrl;

  @NotNull
  public String clientId;

  @NotNull
  public String issuer;

  public String audience;

}