package com.grahamcrockford.oco.api.auth;

import io.dropwizard.Configuration;
import javax.validation.constraints.NotNull;

public class AuthConfiguration extends Configuration {

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