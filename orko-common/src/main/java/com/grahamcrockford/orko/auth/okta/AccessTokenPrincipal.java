package com.grahamcrockford.orko.auth.okta;

import java.security.Principal;

import com.okta.jwt.Jwt;

public class AccessTokenPrincipal implements Principal {

  private final Jwt accessToken;

  public AccessTokenPrincipal(Jwt accessToken) {
      this.accessToken = accessToken;
  }

  @Override
  public String getName() {
      // the 'sub' claim in the access token will be the email address
      return (String) accessToken.getClaims().get("sub");
  }
}