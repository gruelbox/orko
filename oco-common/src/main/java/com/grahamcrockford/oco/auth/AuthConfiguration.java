package com.grahamcrockford.oco.auth;

import javax.validation.constraints.NotNull;

public class AuthConfiguration {

  /**
   * Okta configuration.
   */
  public OktaConfiguration okta;

  /**
   * If set, is the 2FA secret key for IP whitelisting. If disabled, so is IP
   * whitelisting.
   */
  public String secretKey;

  /**
   * How long whitelisting should live for.
   */
  public Integer whitelistExpirySeconds = 28800;

  /**
   * Set to {@code true} on Heroku so it uses the `X-Forwarded-For` header to
   * determine the source IP. This MUST be {@code false} if you're not hosted behind a
   * trusted proxy where you can 100% believe the `X-Forwarded-For` header, or
   * someone could easily spoof their IP and bypass your 2FA.
   */
  @NotNull
  public boolean proxied;

  public String authCachePolicy = "maximumSize=10000, expireAfterAccess=10m";
}