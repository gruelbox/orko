package com.grahamcrockford.oco.auth;

import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;

public interface AuthenticatorAuthoriser extends Authenticator<String, AccessTokenPrincipal>, Authorizer<AccessTokenPrincipal>  {

}
