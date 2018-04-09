package com.grahamcrockford.oco.api.auth;

import io.dropwizard.Configuration;
import javax.validation.constraints.NotNull;

public class OktaConfiguration extends Configuration {

  @NotNull
  public String baseUrl;

  @NotNull
  public String clientId;

  @NotNull
  public String issuer;

  public String audience;

}