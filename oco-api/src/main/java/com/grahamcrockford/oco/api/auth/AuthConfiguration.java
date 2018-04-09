package com.grahamcrockford.oco.api.auth;

import javax.validation.constraints.NotNull;

public class AuthConfiguration {

  public OktaConfiguration okta;

  @NotNull
  public String adminUserName;

  @NotNull
  public String adminPassword;

  public String secretKey;

  public Integer whitelistExpirySeconds;

  @NotNull
  public boolean proxied;
}