package com.grahamcrockford.oco.auth;

import javax.validation.constraints.NotNull;

public class AuthConfiguration {

  public OktaConfiguration okta;

  @NotNull
  public String adminUserName;

  @NotNull
  public String adminPassword;

  public String secretKey;

  public Integer whitelistExpirySeconds = 28800;

  @NotNull
  public boolean proxied;

  public String authCachePolicy = "maximumSize=10000, expireAfterAccess=10m";
}